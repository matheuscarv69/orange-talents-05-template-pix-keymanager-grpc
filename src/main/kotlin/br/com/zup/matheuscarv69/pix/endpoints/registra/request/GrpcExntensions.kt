package br.com.zup.matheuscarv69.pix.endpoints.registra.request

import br.com.zup.matheuscarv69.RegistraChavePixRequest
import br.com.zup.matheuscarv69.TipoDeChaveGrpc
import br.com.zup.matheuscarv69.TipoDeContaGrpc
import br.com.zup.matheuscarv69.pix.entities.chave.TipoDeChave
import br.com.zup.matheuscarv69.pix.entities.chave.TipoDeConta

fun RegistraChavePixRequest.toModel(): NovaChaveRequest {
    return NovaChaveRequest(
        clienteId = clienteId,
        tipoDeChave = when (tipoDeChave) {
            TipoDeChaveGrpc.TIPO_DE_CHAVE_DESCONHECIDA -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            TipoDeContaGrpc.TIPO_CONTA_DESCONHECIDA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )

}