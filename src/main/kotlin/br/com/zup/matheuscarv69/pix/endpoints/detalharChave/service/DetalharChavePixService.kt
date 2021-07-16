package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.service

import br.com.zup.matheuscarv69.DetalharChavePixResponse
import br.com.zup.matheuscarv69.clients.bcb.BcbClient
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixNotFoundException
import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.request.DetalharChaveRequest
import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.response.ChavePixResponse
import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.response.ConverterChavePixToDetalharChavePixResponse
import br.com.zup.matheuscarv69.pix.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

@Validated
@Singleton
class DetalharChavePixService(
    @Inject private val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient,
    @Inject val validator: Validator
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun detalhar(request: DetalharChaveRequest): DetalharChavePixResponse {
        // valida se a busca vai ser por pixId
        val detalharPorPixId = validaRequest(request)

        // 1. verificar se a busca eh por pixId e clientId
        if (detalharPorPixId) {
            LOGGER.info("Realizando busca por PixId e ClienteId")

            val chavePix = repository.findByPixIdAndClienteId(
                pixId = request.pixId!!,
                clienteId = UUID.fromString(request.clienteId)
            ).orElseThrow { throw ChavePixNotFoundException("Chave Pix não foi encontrada") }

            // 2. enviar request para o bcb com a chave pix
            val bcbDetalheResponse = bcbClient.buscaPorChavePix(chavePix.chave).body()
                ?: throw IllegalStateException("Erro ao fazer busca de chave no Banco Central do Brasil (BCB")

            // 3. verifica se a chave da response do bcb pertence ao cliente pelo cpf
            if (chavePix.conta.cpfDoTitular != bcbDetalheResponse.owner.taxIdNumber)
                throw IllegalStateException("Chave não pertence ao cliente informado")

            // 4. converte chave pix e retornar os detalhes da chave pix
            val chavePixResponse = ChavePixResponse.of(chavePix)
            return ConverterChavePixToDetalharChavePixResponse().converter(chavePixResponse)
        }

        LOGGER.info("Realizando busca por Chave")
        val chavePix = repository.findByChave(request.chave!!)

        if (chavePix.isEmpty) {
            val bcbDetalheResponse = bcbClient.buscaPorChavePix(request.chave).body()
                ?: throw IllegalStateException("Erro ao fazer busca de chave no Banco Central do Brasil (BCB")
            val chavePixResponse = bcbDetalheResponse.toModel()
            return ConverterChavePixToDetalharChavePixResponse().converter(chavePixResponse)
        }

        return ConverterChavePixToDetalharChavePixResponse().converter(ChavePixResponse.of(chavePix.get()))
    }

    private fun validaRequest(request: DetalharChaveRequest): Boolean {
        val detalharPorPixId = request.pixId!!.isNotEmpty() && request.clienteId!!.isNotEmpty()

        return if (detalharPorPixId) {
            val violations = validator.validate(request)
            if (violations.isNotEmpty())
                throw ConstraintViolationException(violations)
            detalharPorPixId
        } else {
            val violations = validator.validate(request.chave!!)
            if (violations.isNotEmpty())
                throw ConstraintViolationException(violations)
            if (request.chave.isEmpty())
                throw IllegalStateException("A Chave está vazia")
            detalharPorPixId
        }

    }

}
