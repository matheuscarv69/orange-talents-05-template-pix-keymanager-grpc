package br.com.zup.matheuscarv69.pix.endpoints.detalharChave

import br.com.zup.matheuscarv69.DetalharChavePixRequest
import br.com.zup.matheuscarv69.DetalharChavePixResponse
import br.com.zup.matheuscarv69.KeyManagerDetalhaChaveServiceGrpc
import br.com.zup.matheuscarv69.core.errorsHandler.ErrorHandler
import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.request.toModel
import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.service.DetalharChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DetalharChaveEndpoint(@Inject private val service: DetalharChavePixService) :
    KeyManagerDetalhaChaveServiceGrpc.KeyManagerDetalhaChaveServiceImplBase() {

    override fun detalhar(
        request: DetalharChavePixRequest,
        responseObserver: StreamObserver<DetalharChavePixResponse>
    ) {

        val detalharRequest = request.toModel()
        val detalhesChave = service.detalhar(detalharRequest)

        responseObserver.onNext(detalhesChave)
        responseObserver.onCompleted()

    }


}