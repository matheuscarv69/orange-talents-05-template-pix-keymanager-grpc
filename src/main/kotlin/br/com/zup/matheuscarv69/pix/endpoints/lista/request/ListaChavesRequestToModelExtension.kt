package br.com.zup.matheuscarv69.pix.endpoints.lista.request
import br.com.zup.matheuscarv69.ListaChavesRequest

fun ListaChavesRequest.toModel(): ListaRequest {
    return ListaRequest(clienteId = clienteId)
}