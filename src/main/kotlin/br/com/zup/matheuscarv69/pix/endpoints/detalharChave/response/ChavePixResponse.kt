package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.response

import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
import br.com.zup.matheuscarv69.pix.entities.chave.ContaAssociada
import br.com.zup.matheuscarv69.pix.entities.chave.TipoDeChave
import br.com.zup.matheuscarv69.pix.entities.chave.TipoDeConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixResponse(
    val pixId: String? = UUID.randomUUID().toString(),
    val clienteId: String?,
    val tipoDeChave: TipoDeChave,
    var chave: String,
    val tipoDeConta: TipoDeConta,
    val conta: ContaAssociada,
    val criadoEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixResponse {
            return ChavePixResponse(
                pixId = chave.pixId,
                clienteId = chave.clienteId.toString(),
                tipoDeChave = chave.tipoDeChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                criadoEm = chave.criadoEm
            )
        }
    }

}