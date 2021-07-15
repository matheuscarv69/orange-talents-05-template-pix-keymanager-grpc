package br.com.zup.matheuscarv69.pix.endpoints.remover.service

import br.com.zup.matheuscarv69.clients.bcb.BcbClient
import br.com.zup.matheuscarv69.clients.bcb.DeletePixKeyRequest
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixNaoPertenceAoClienteException
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixNotFoundException
import br.com.zup.matheuscarv69.pix.endpoints.remover.request.RemoverChaveRequest
import br.com.zup.matheuscarv69.pix.repositories.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoverChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remover(@Valid removerChaveRequest: RemoverChaveRequest) {
        val possibleChavePix = repository.findByPixId(removerChaveRequest.pixId)

        // 1. Verificar se o pixId informado existe
        if (possibleChavePix.isEmpty)
            throw ChavePixNotFoundException("Chave Pix não encontrada")

        val chavePix = possibleChavePix.get()

        // 2. Verificar se a chave pertence ao cliente
        if (!chavePix.pertenceAoCliente(removerChaveRequest.clienteId))
            throw ChavePixNaoPertenceAoClienteException("Chave Pix não pertence ao Cliente informado")

        // 3. Excluir chave do BCB
        val bcbResponse = bcbClient.deletarPix(
            key = chavePix.chave,
            request = DeletePixKeyRequest(
                key = chavePix.chave,
                participant = chavePix.conta!!.ispb
            )
        ).also {
            LOGGER.info("Removendo chave Pix no Banco Central do Brasil: ${chavePix.chave}")
        }

        if (bcbResponse.status != HttpStatus.OK)
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")

        // 4. Excluir a chave pix do banco
        LOGGER.info("Excluindo Chave Pix ${chavePix.tipoDeChave.name} : ${chavePix.pixId} ")
        repository.deleteById(chavePix.id!!)
    }

}
