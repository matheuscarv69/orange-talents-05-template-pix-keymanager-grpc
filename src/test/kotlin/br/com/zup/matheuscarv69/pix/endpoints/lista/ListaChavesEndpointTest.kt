package br.com.zup.matheuscarv69.pix.endpoints.lista

import br.com.zup.matheuscarv69.KeyManagerListaChavesServiceGrpc
import br.com.zup.matheuscarv69.ListaChavesRequest
import br.com.zup.matheuscarv69.TipoDeContaGrpc
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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class ListaChavesEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerListaChavesServiceGrpc.KeyManagerListaChavesServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = UUID.randomUUID()!!
    }

    @BeforeEach
    fun setUp() {
        repository.save(criaChavePix())
        repository.save(criaChavePix(TipoDeChave.CPF, "70591162067"))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    /**
     *
     * 1. Deve listar as chaves de um cliente
     * 2. Deve retornar o Cliente Id e uma lista vazia
     * 3. Nao deve listar quando o Cliente Id nao for valido
     * */

    @Test
    fun `Deve listar as chaves de um cliente`() {

        // cenario
        val listaDeChaves = repository.findAllByClienteId(CLIENT_ID)
        val chave1 = listaDeChaves[0]
        val chave2 = listaDeChaves[1]

        // acao
        val response = grpcClient.lista(
            ListaChavesRequest
                .newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .build()
        )

        //validacao
        with(response.chavesList) {
            assertEquals(listaDeChaves.size, this.size)

            val chaveResponse1 = this[0]
            val chaveResponse2 = this[1]

            assertEquals(chave1.tipoDeChave.name, chaveResponse1.tipoDeChave.name)
            assertEquals(chave1.chave, chaveResponse1.chave)
            assertEquals(chave1.pixId, chaveResponse1.pixId)
            assertEquals(chave1.tipoDeConta.name, chaveResponse1.tipoDeConta.name)

            assertEquals(chave2.tipoDeChave.name, chaveResponse2.tipoDeChave.name)
            assertEquals(chave2.chave, chaveResponse2.chave)
            assertEquals(chave2.pixId, chaveResponse2.pixId)
            assertEquals(chave2.tipoDeConta.name, chaveResponse2.tipoDeConta.name)
        }

    }

    @Test
    fun `Deve retornar o Cliente Id e uma lista vazia`() {

        // cenario
        val clienteId = UUID.randomUUID().toString()

        // acao
        val response = grpcClient.lista(
            ListaChavesRequest
                .newBuilder()
                .setClienteId(clienteId)
                .build()
        )

        // validacao
        with(response) {
            assertEquals(clienteId, this.clienteId)
            assertEquals(0, this.chavesCount)
        }

    }

    @Test
    fun `Nao deve listar quando o Cliente Id nao for valido`() {

        // cenario

        // acao
        val errors = assertThrows<StatusRuntimeException> {
            val response = grpcClient.lista(
                ListaChavesRequest
                    .newBuilder()
                    .setClienteId("Quero descansar! ='( ")
                    .build()
            )
        }

        // validacao
        with(errors) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }

    }

    // criacao de dados

    private fun criaChavePix(tipoDeChave: TipoDeChave = TipoDeChave.EMAIL, chave: String = "rafa.ponte@zup.com.br") =
        ChavePix(
            clienteId = CLIENT_ID,
            tipoDeChave = tipoDeChave,
            chave = chave,
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


    @Factory
    class Client() {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerListaChavesServiceGrpc.KeyManagerListaChavesServiceBlockingStub? {
            return KeyManagerListaChavesServiceGrpc.newBlockingStub(channel)
        }
    }

}