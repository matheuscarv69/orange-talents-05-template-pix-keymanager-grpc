package br.com.zup.matheuscarv69.pix.endpoints.registra.service

import br.com.zup.matheuscarv69.clients.itau.ContasDeClientesItauClient
import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixExistenteException
import br.com.zup.matheuscarv69.pix.endpoints.registra.request.NovaChaveRequest
import br.com.zup.matheuscarv69.pix.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientesItauClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChaveRequest): ChavePix {

        // 1. verifica se chave já existe no sistema
        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        // 2. busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        // 3. grava no banco de dados
        val chave = novaChave.toModel(conta)
        repository.save(chave)

        return chave
    }


}
