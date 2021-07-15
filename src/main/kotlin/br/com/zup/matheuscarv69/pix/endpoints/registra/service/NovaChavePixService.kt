package br.com.zup.matheuscarv69.pix.endpoints.registra.service

import br.com.zup.matheuscarv69.clients.bcb.BcbClient
import br.com.zup.matheuscarv69.clients.bcb.CreatePixKeyRequest
import br.com.zup.matheuscarv69.clients.itau.ContasDeClientesItauClient
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixExistenteException
import br.com.zup.matheuscarv69.pix.endpoints.registra.request.NovaChaveRequest
import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
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
    @Inject val itauClient: ContasDeClientesItauClient,
    @Inject val bcbClient: BcbClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChaveRequest): ChavePix {

        // 1. verifica se chave já existe no sistema
        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")

        // 2. busca dados da conta no ERP do ITAU
        val itauResponse = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = itauResponse.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        // 3. grava chave no banco de dados
        val chave = novaChave.toModel(conta)
        repository.save(chave)

        // 4. envia chave para o sistema do BCB
        val request = CreatePixKeyRequest.of(chave).also {
            LOGGER.info("Registrando chave Pix no Banco Central do Brasil: ${chave.chave}")
        }

        val bcbResponse = bcbClient.registraPix(request).body()
            ?: throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")


        // 5. atualiza chave com valor retornado do bcb
        chave.atualizaChave(bcbResponse.key)

        // save explicito, nao necessario pois a chave esta em estado de managed
        repository.save(chave)

        return chave
    }


}
