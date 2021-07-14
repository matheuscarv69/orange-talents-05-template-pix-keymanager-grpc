package br.com.zup.matheuscarv69.pix.endpoints.remover

import br.com.zup.matheuscarv69.KeyManagerRemoverServiceGrpc
import br.com.zup.matheuscarv69.RemoverChavePixRequest
import br.com.zup.matheuscarv69.RemoverChavePixResponse
import br.com.zup.matheuscarv69.core.errorsHandler.ErrorHandler
import br.com.zup.matheuscarv69.pix.endpoints.remover.request.toModel
import br.com.zup.matheuscarv69.pix.endpoints.remover.service.RemoverChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoverChaveEndpoint(@Inject private val service: RemoverChavePixService) :
    KeyManagerRemoverServiceGrpc.KeyManagerRemoverServiceImplBase() {

    override fun remover(
        request: RemoverChavePixRequest,
        responseObserver: StreamObserver<RemoverChavePixResponse>
    ) {

        val removerChaveRequest = request.toModel()
        service.remover(removerChaveRequest)

        responseObserver.onNext(
            RemoverChavePixResponse
                .newBuilder()
                .setMensagem("Chave excluída com sucesso")
                .build()
        )

        responseObserver.onCompleted()
    }
}