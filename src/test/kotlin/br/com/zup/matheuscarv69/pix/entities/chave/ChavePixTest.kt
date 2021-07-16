package br.com.zup.matheuscarv69.pix.entities.chave

import br.com.zup.matheuscarv69.TipoDeContaGrpc
import br.com.zup.matheuscarv69.clients.itau.DadosDaContaResponse
import br.com.zup.matheuscarv69.clients.itau.InstituicaoResponse
import br.com.zup.matheuscarv69.clients.itau.TitularResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {

    companion object {
        val CLIENT_ID = UUID.randomUUID()!!
    }

    // 1. Deve ser valido quando a Chave pertencer ao Cliente informado
    // 2. Nao deve ser valido quando a Chave nao pertencer ao Cliente informado

    @Test
    fun `Deve ser valido quando a Chave pertencer ao Cliente informado`() {

        // cenario
        val chavePix = criaChavePix()

        // acao
        val chavePertenceAoCliente = chavePix.pertenceAoCliente(clienteId = CLIENT_ID.toString())

        // validacao
        assertTrue(chavePertenceAoCliente)
    }

    @Test
    fun `Nao deve ser valido quando a Chave nao pertencer ao Cliente informado`() {
        // cenario
        val chavePix = criaChavePix()

        // acao
        val chaveNaoPertenceAoCliente = chavePix.pertenceAoCliente(clienteId = UUID.randomUUID().toString())
        val chavePertenceAoCliente = chavePix.pertenceAoCliente(clienteId = chavePix.clienteId.toString())

        // validacao
        assertFalse(chaveNaoPertenceAoCliente)
        assertTrue(chavePertenceAoCliente)
    }

    @Test
    fun `Nao deve ser possivel atualizar a chave quando ela nao for do tipo ALEATORIA`() {

        // cenario
        val chavePix = criaChavePix()
        val novaChave = "12345678910"

        // acao
        val isPossible = chavePix.atualizaChave(novaChave)

        // validacao
        assertNotNull(isPossible)
        assertFalse(isPossible)

    }

    @Test
    fun `Deve ser possivel atualizar a chave quando ela for do tipo ALEATORIA`() {
        // cenario
        val chavePix = ChavePix(
            clienteId = CLIENT_ID,
            tipoDeChave = TipoDeChave.ALEATORIA,
            chave = "",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = dadosDaContaResponse().toModel()
        )
        val novaChave = "12345678910"

        // acao
        val isPossible = chavePix.atualizaChave(novaChave)

        // validacao
        assertNotNull(isPossible)
        assertTrue(isPossible)
    }

    @Test
    fun `Deve ser verdadeiro quando a chave for do tipo ALEATORIA`() {
        // cenario
        val chavePix = ChavePix(
            clienteId = CLIENT_ID,
            tipoDeChave = TipoDeChave.ALEATORIA,
            chave = "",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = dadosDaContaResponse().toModel()
        )
        // acao
        val isAleatoria = chavePix.isAleatoria()

        // validacao
        assertNotNull(isAleatoria)
        assertTrue(isAleatoria)
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

}