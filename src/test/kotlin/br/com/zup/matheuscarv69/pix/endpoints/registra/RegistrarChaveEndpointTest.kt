package br.com.zup.matheuscarv69.pix.endpoints.registra

import br.com.zup.matheuscarv69.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.matheuscarv69.RegistraChavePixRequest
import br.com.zup.matheuscarv69.TipoDeChaveGrpc
import br.com.zup.matheuscarv69.TipoDeContaGrpc
import br.com.zup.matheuscarv69.clients.bcb.*
import br.com.zup.matheuscarv69.clients.itau.ContasDeClientesItauClient
import br.com.zup.matheuscarv69.clients.itau.DadosDaContaResponse
import br.com.zup.matheuscarv69.clients.itau.InstituicaoResponse
import br.com.zup.matheuscarv69.clients.itau.TitularResponse
import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
import br.com.zup.matheuscarv69.pix.entities.chave.ContaAssociada
import br.com.zup.matheuscarv69.pix.entities.chave.TipoDeChave
import br.com.zup.matheuscarv69.pix.entities.chave.TipoDeConta
import br.com.zup.matheuscarv69.pix.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistrarChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ContasDeClientesItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()!!
    }

    /**
     * 1. Cenario feliz - OK
     * 2. Nao deve registrar quando a chave pix ja existe - OK
     * 3. Nao deve registrar chave pix quando o cliente nao for encontrado - OK
     * 4. Nao deve registrar chave pix quando os parametros estiverem invalidos - OK
     * */

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `Deve cadastrar a chave pix`() {
        // Cenario
        Mockito.`when`(itauClient.buscaContaPorTipo(CLIENT_ID.toString(), TipoDeConta.CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        Mockito.`when`(bcbClient.registraPix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // acao
        val response = grpcClient.registrar(
            RegistraChavePixRequest
                .newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChaveGrpc.CPF)
                .setChave("02467781054")
                .setTipoDeConta(TipoDeContaGrpc.CONTA_CORRENTE)
                .build()
        )


        // validacao
        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByPixId(pixId))
        }
    }

    @Test
    fun `Nao deve registrar quando a chave pix ja existe`() {
        // cenario
        repository.save(
            ChavePix(
                clienteId = CLIENT_ID,
                tipoDeChave = TipoDeChave.EMAIL,
                chave = "rafa.ponte@zup.com.br",
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = dadosDaContaResponse().toModel()
            )
        )

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChaveGrpc.EMAIL)
                    .setChave("rafa.ponte@zup.com.br")
                    .setTipoDeConta(TipoDeContaGrpc.CONTA_CORRENTE)
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix 'rafa.ponte@zup.com.br' existente", status.description)
        }

    }

    @Test
    fun `Nao deve registrar chave pix quando o cliente nao for encontrado`() {

        // cenario
        Mockito.`when`(
            itauClient.buscaContaPorTipo(
                clienteId = CLIENT_ID.toString(),
                tipo = TipoDeConta.CONTA_CORRENTE.name
            )
        ).thenReturn(HttpResponse.notFound())

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChaveGrpc.EMAIL)
                    .setChave("rafa.ponte@zup.com.br")
                    .setTipoDeConta(TipoDeContaGrpc.CONTA_CORRENTE)
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }

    }

    @Test
    fun `Nao deve registrar chave pix quando os parametros estiverem invalidos`() {
        // cenario

        // acao
        val erros = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(RegistraChavePixRequest.newBuilder().build())
        }

        // validacao

        with(erros) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }

    }

    // Mockando o itau client
    @MockBean(ContasDeClientesItauClient::class)
    fun itauClientMock(): ContasDeClientesItauClient {
        return Mockito.mock(ContasDeClientesItauClient::class.java)
    }

    // Mockando o bcb client
    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    // cria um client grpc pro endpoint de Registrar chave pix
    @Factory
    class Clients() {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub? {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    // response itau request
    private fun dadosDaContaResponse() = DadosDaContaResponse(
        tipo = TipoDeContaGrpc.CONTA_CORRENTE.name,
        instituicao = InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", ispb = ContaAssociada.ITAU_UNIBANCO_ISPB),
        agencia = "0001",
        numero = "291900",
        titular = TitularResponse(nome = "Rafael Ponte", cpf = "02467781054")
    )


    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = PixKeyType.CPF,
            key = "02467781054",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = PixKeyType.CPF,
            key = "02467781054",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "0001",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "02467781054"
        )
    }
}