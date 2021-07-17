package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.service

import br.com.zup.matheuscarv69.DetalharChavePixResponse
import br.com.zup.matheuscarv69.clients.bcb.BcbClient
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixNaoPertenceAoClienteException
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
        val detalharPorPixId = ValidaDetalharRequest.validaRequest(request, validator)

        // 1. verificar se a busca eh por pixId e clientId
        if (detalharPorPixId) {
            LOGGER.info("Realizando busca por PixId e ClienteId")

            val chavePix = repository.findByPixIdAndClienteId(
                pixId = request.pixId!!,
                clienteId = UUID.fromString(request.clienteId)
            ).orElseThrow { throw ChavePixNotFoundException("Chave Pix não foi encontrada") }

            // 2. enviar request para o bcb com a chave pix
            val bcbDetalheResponse = bcbClient.buscaPorChavePix(chavePix.chave).body()
                ?: throw ChavePixNotFoundException("Erro ao fazer busca de chave no Banco Central do Brasil (BCB), chave não encontrada")

            // 3. verifica se a chave da response do bcb pertence ao cliente pelo cpf
            if (chavePix.conta.cpfDoTitular != bcbDetalheResponse.owner.taxIdNumber)
                throw ChavePixNaoPertenceAoClienteException("Chave não pertence ao cliente informado")

            // 4. converte chave pix e retornar os detalhes da chave pix
            val chavePixResponse = ChavePixResponse.of(chavePix)
            return ConverterChavePixToDetalharChavePixResponse().converter(chavePixResponse)
        }

        // Consulta via chave
        LOGGER.info("Realizando busca por Chave")

        // 5. busca no banco pela chave
        val chavePix = repository.findByChave(request.chave!!)

        // 6. caso a chave nao seja encontrada no banco a consulta eh feita no bcb
        if (chavePix.isEmpty) {
            LOGGER.info("Chave não foi encontrada no BD, fazendo consulta ao BCB")

            val bcbDetalheResponse = bcbClient.buscaPorChavePix(request.chave).body()
                ?: throw ChavePixNotFoundException("Erro ao fazer busca de chave no Banco Central do Brasil (BCB), chave não encontrada")
            val chavePixResponse = bcbDetalheResponse.toModel()
            return ConverterChavePixToDetalharChavePixResponse().converter(chavePixResponse)
        }

        return ConverterChavePixToDetalharChavePixResponse().converter(ChavePixResponse.of(chavePix.get()))
    }

}
