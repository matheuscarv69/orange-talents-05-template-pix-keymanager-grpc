package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.service

import br.com.zup.matheuscarv69.DetalharChavePixRequest
import br.com.zup.matheuscarv69.KeyManagerDetalhaChaveServiceGrpc
import br.com.zup.matheuscarv69.TipoDeChaveGrpc
import br.com.zup.matheuscarv69.TipoDeContaGrpc
import br.com.zup.matheuscarv69.clients.bcb.*
import br.com.zup.matheuscarv69.clients.itau.DadosDaContaResponse
import br.com.zup.matheuscarv69.clients.itau.InstituicaoResponse
import br.com.zup.matheuscarv69.clients.itau.TitularResponse
import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
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
import org.junit.jupiter.api.AfterEach
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
internal class DetalharChavePixServiceTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerDetalhaChaveServiceGrpc.KeyManagerDetalhaChaveServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val PIX_ID = UUID.randomUUID()!!
        val CLIENT_ID = UUID.randomUUID()!!
        val EMAIL_YURI = "yuri.matheus@zup.com.br"
    }

    @BeforeEach
    fun setUp() {
        repository.save(criaChavePix())
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    /**
     * 1. Deve detalhar chave por PixId e ClienteId - ok
     * 2. Deve detalhar chave por chave - ok
     * 3. Deve detalhar chave quando ela nao existir no banco local, mas existir no BCB
     * 4. Nao deve detalhar quando a chave nao for encontrada na busca por PixId e ClienteId - ok
     * 5. Nao deve detalhar quando ocorrer erro na busca no Banco Central do Brasil -ok
     * 6. Nao deve detalhar quando a chave nao pertencer ao cliente informado - ok
     * 7. Nao deve detalhar quando a consulta por chave quando ocorrer error no BCB - ok
     */

    @Test
    fun `Deve detalhar chave por PixId e ClienteId`() {

        // cenario
        val chavePix = repository.findByChave(chave = "rafa.ponte@zup.com.br").get()

        Mockito.`when`(bcbClient.buscaPorChavePix(chavePix.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // acao
        val detalharResponse = grpcClient.detalhar(
            DetalharChavePixRequest
                .newBuilder()
                .setPixIdEClienteId(
                    DetalharChavePixRequest.ChavePixRequest
                        .newBuilder()
                        .setPixId(chavePix.pixId)
                        .setClienteId(chavePix.clienteId.toString())
                        .build()
                )
                .build()
        )

        // validacao
        with(detalharResponse) {
            assertNotNull(this)
            assertEquals(chavePix.pixId, this.pixId)
            assertEquals(chavePix.clienteId.toString(), this.clienteId.toString())
            assertEquals(chavePix.chave, this.chavePix.chave)
            assertEquals(chavePix.tipoDeChave.name, this.chavePix.tipoDeChave.name)
        }

    }

    @Test
    fun `Deve detalhar chave por Chave Pix informada`() {

        // cenario
        val chavePix = repository.findByChave(chave = "rafa.ponte@zup.com.br").get()

        Mockito.`when`(bcbClient.buscaPorChavePix(chavePix.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // acao
        val detalharResponse = grpcClient.detalhar(
            DetalharChavePixRequest
                .newBuilder()
                .setPixIdEClienteId(
                    DetalharChavePixRequest.ChavePixRequest
                        .newBuilder()
                        .build()
                )
                .setChave(chavePix.chave)
                .build()
        )

        // validacao

        with(detalharResponse) {
            assertNotNull(this)
            assertEquals(chavePix.pixId, this.pixId)
            assertEquals(chavePix.clienteId.toString(), this.clienteId.toString())
            assertEquals(chavePix.chave, this.chavePix.chave)
            assertEquals(chavePix.tipoDeChave.name, this.chavePix.tipoDeChave.name)
        }

    }

    @Test
    fun `Deve detalhar chave quando ela nao existir no banco local, mas existir no BCB`() {

        // cenario
        val pixKeyDetailsResponse = pixKeyDetailsResponse()
        pixKeyDetailsResponse.key = EMAIL_YURI

        Mockito.`when`(bcbClient.buscaPorChavePix(EMAIL_YURI))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse))

        // acao
        val detalharResponse = grpcClient.detalhar(
            DetalharChavePixRequest
                .newBuilder()
                .setPixIdEClienteId(
                    DetalharChavePixRequest.ChavePixRequest
                        .newBuilder()
                        .build()
                )
                .setChave(EMAIL_YURI)
                .build()
        )

        // validacao

        with(detalharResponse) {
            assertNotNull(this)
            assertEquals(EMAIL_YURI, this.chavePix.chave)
            assertEquals(TipoDeChave.EMAIL.name, this.chavePix.tipoDeChave.name)
        }

    }

    @Test
    fun `Nao deve detalhar quando a chave nao for encontrada na busca por PixId e ClienteId`() {

        // cenario

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            val response = grpcClient.detalhar(
                DetalharChavePixRequest
                    .newBuilder()
                    .setPixIdEClienteId(
                        DetalharChavePixRequest.ChavePixRequest
                            .newBuilder()
                            .setPixId(PIX_ID.toString())
                            .setClienteId(CLIENT_ID.toString())
                            .build()
                    )
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não foi encontrada", status.description)
        }
    }

    @Test
    fun `Nao deve detalhar quando ocorrer erro na busca no Banco Central do Brasil`() {

        // cenario
        val chavePix = repository.findByChave(chave = "rafa.ponte@zup.com.br").get()

        Mockito.`when`(bcbClient.buscaPorChavePix(chavePix.chave))
            .thenReturn(HttpResponse.notFound())

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            val response = grpcClient.detalhar(
                DetalharChavePixRequest
                    .newBuilder()
                    .setPixIdEClienteId(
                        DetalharChavePixRequest.ChavePixRequest
                            .newBuilder()
                            .setPixId(chavePix.pixId)
                            .setClienteId(chavePix.clienteId.toString())
                            .build()
                    )
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(
                "Erro ao fazer busca de chave no Banco Central do Brasil (BCB), chave não encontrada",
                status.description
            )
        }

    }

    @Test
    fun `Nao deve detalhar quando a chave nao pertencer ao cliente informado`() {

        // cenario
        val chavePix = repository.findByChave(chave = "rafa.ponte@zup.com.br").get()

        val pixKeyDetailsResponse = pixKeyDetailsResponse()
        pixKeyDetailsResponse.owner.taxIdNumber = "12345678910" // cpf aleatorio

        Mockito.`when`(bcbClient.buscaPorChavePix(chavePix.chave))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse))

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            val response = grpcClient.detalhar(
                DetalharChavePixRequest
                    .newBuilder()
                    .setPixIdEClienteId(
                        DetalharChavePixRequest.ChavePixRequest
                            .newBuilder()
                            .setPixId(chavePix.pixId)
                            .setClienteId(chavePix.clienteId.toString())
                            .build()
                    )
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave não pertence ao cliente informado", status.description)
        }
    }

    @Test
    fun `Nao deve detalhar quando a consulta por chave quando ocorrer error no BCB`() {

        // cenario
        Mockito.`when`(bcbClient.buscaPorChavePix(EMAIL_YURI))
            .thenReturn(HttpResponse.notFound())

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            val response = grpcClient.detalhar(
                DetalharChavePixRequest
                    .newBuilder()
                    .setPixIdEClienteId(
                        DetalharChavePixRequest.ChavePixRequest
                            .newBuilder()
                            .build()
                    )
                    .setChave(EMAIL_YURI)
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(
                "Erro ao fazer busca de chave no Banco Central do Brasil (BCB), chave não encontrada",
                status.description
            )
        }
    }

    // criacao de dados

    private fun criaChavePix() = ChavePix(
        clienteId = CLIENT_ID,
        tipoDeChave = TipoDeChave.EMAIL,
        chave = "rafa.ponte@zup.com.br",
        tipoDeConta = TipoDeConta.CONTA_CORRENTE,
        conta = dadosDaContaResponse().toModel()
    )

    private fun dadosDaContaResponse() = DadosDaContaResponse(
        tipo = TipoDeContaGrpc.CONTA_CORRENTE.name,
        instituicao = InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
        agencia = "0001",
        numero = "291900",
        titular = TitularResponse(nome = "Rafael M C Ponte", cpf = "02467781054")
    )


    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = "rafa.ponte@zup.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "60701190",
            branch = "0001",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael M C Ponte",
            taxIdNumber = "02467781054"
        )
    }


    // mocks

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Client() {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerDetalhaChaveServiceGrpc.KeyManagerDetalhaChaveServiceBlockingStub? {
            return KeyManagerDetalhaChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}