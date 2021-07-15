package br.com.zup.matheuscarv69.pix.endpoints.remover

import br.com.zup.matheuscarv69.KeyManagerRemoverServiceGrpc
import br.com.zup.matheuscarv69.RemoverChavePixRequest
import br.com.zup.matheuscarv69.TipoDeContaGrpc
import br.com.zup.matheuscarv69.clients.bcb.BcbClient
import br.com.zup.matheuscarv69.clients.bcb.DeletePixKeyRequest
import br.com.zup.matheuscarv69.clients.bcb.DeletePixKeyResponse
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoverChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerRemoverServiceGrpc.KeyManagerRemoverServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()!!
    }

    /**
     * 1. Cenario Feliz - OK
     * 2. Nao deve deletar chave com PixId nao encontrado - OK
     * 3. Nao deve deletar chave quando ela nao pertencer ao cliente informado - ok
     * 4. Nao deve deletar quando os dados informados forem invalidos
     * */

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `Deve remover chave pix`() {

        // cenario
        val chavePix = repository.save(criaChavePix())

        Mockito.`when`(bcbClient.deletarPix(chavePix.chave, deletePixKeyRequest(chavePix.chave)))
            .thenReturn(HttpResponse.ok(deletePixKeyResponse(chavePix.chave)))

        // acao
        val response = grpcClient.remover(
            RemoverChavePixRequest
                .newBuilder()
                .setPixId(chavePix.pixId)
                .setClienteId(chavePix.clienteId.toString())
                .build()
        )

        // validacao
        with(response) {
            assertNotNull(response)
            assertEquals("Chave excluída com sucesso", response.mensagem)
        }

    }

    @Test
    fun `Nao deve deletar chave quando ocorrer erro no BcbClient`() {
        // cenario
        val chavePix = repository.save(criaChavePix())

        Mockito.`when`(bcbClient.deletarPix(chavePix.chave, deletePixKeyRequest(chavePix.chave)))
            .thenReturn(HttpResponse.unprocessableEntity())

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            val response = grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setPixId(chavePix.pixId)
                    .setClienteId(chavePix.clienteId.toString())
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }

    @Test
    fun `Nao deve deletar chave quando o PixId nao for encontrado`() {
        // cenario
        // acao
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setPixId("075bbbc3-e550-4979-8a79-32fd9ae7fd67")
                    .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `Nao deve deletar chave quando ela nao pertencer ao cliente informado`() {
        // cenario
        val chavePix = repository.save(criaChavePix())

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setPixId(chavePix.pixId)
                    .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave Pix não pertence ao Cliente informado", status.description)
        }
    }

    @Test
    fun `Nao deve deletar quando os dados informados forem invalidos`() {
        // cenario

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            grpcClient.remover(
                RemoverChavePixRequest
                    .newBuilder()
                    .setPixId("")
                    .setClienteId("")
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }

    }

    // Mockando o bcb client
    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    // cria um client grpc pro endpoint de Remover chave pix
    @Factory
    class Clients() {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerRemoverServiceGrpc.KeyManagerRemoverServiceBlockingStub? {
            return KeyManagerRemoverServiceGrpc.newBlockingStub(channel)
        }
    }

    // gerando dados
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

    private fun deletePixKeyRequest(chave: String): DeletePixKeyRequest {
        return DeletePixKeyRequest(key = chave)
    }

    private fun deletePixKeyResponse(chave: String): DeletePixKeyResponse {
        return DeletePixKeyResponse(
            key = chave,
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            deletedAt = LocalDateTime.now()
        )
    }

}