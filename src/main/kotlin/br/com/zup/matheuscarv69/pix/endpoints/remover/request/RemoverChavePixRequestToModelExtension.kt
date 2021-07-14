package br.com.zup.matheuscarv69.pix.endpoints.remover.request

import br.com.zup.matheuscarv69.RemoverChavePixRequest

fun RemoverChavePixRequest.toModel(): RemoverChaveRequest {
    return RemoverChaveRequest(
        pixId = pixId,
        clienteId = clienteId
    )
}