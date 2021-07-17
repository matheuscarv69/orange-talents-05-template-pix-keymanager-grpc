package br.com.zup.matheuscarv69.pix.endpoints.lista

import br.com.zup.matheuscarv69.KeyManagerListaChavesServiceGrpc
import br.com.zup.matheuscarv69.ListaChavesRequest
import br.com.zup.matheuscarv69.ListaChavesResponse
import br.com.zup.matheuscarv69.core.errorsHandler.ErrorHandler
import br.com.zup.matheuscarv69.pix.endpoints.lista.request.toModel
import br.com.zup.matheuscarv69.pix.endpoints.lista.service.ListaChavesService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesEndpoint(@Inject private val service: ListaChavesService) :
    KeyManagerListaChavesServiceGrpc.KeyManagerListaChavesServiceImplBase() {

    override fun lista(
        request: ListaChavesRequest,
        responseObserver: StreamObserver<ListaChavesResponse>
    ) {

        val clienteId = request.toModel()
        val listaChavesResponse = service.lista(clienteId)

        responseObserver.onNext(listaChavesResponse)
        responseObserver.onCompleted()

    }

}