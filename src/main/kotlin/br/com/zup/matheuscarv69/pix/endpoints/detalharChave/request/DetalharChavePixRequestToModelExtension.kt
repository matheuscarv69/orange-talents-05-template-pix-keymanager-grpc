package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.request

import br.com.zup.matheuscarv69.DetalharChavePixRequest

fun DetalharChavePixRequest.toModel(): DetalharChaveRequest {
    return DetalharChaveRequest(
        pixId = pixIdEClienteId.pixId,
        clienteId = pixIdEClienteId.clienteId,
        chave = chave
    )
}