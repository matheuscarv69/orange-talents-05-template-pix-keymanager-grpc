package br.com.zup.matheuscarv69.pix.endpoints.registra

import br.com.zup.matheuscarv69.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.matheuscarv69.RegistraChavePixRequest
import br.com.zup.matheuscarv69.RegistraChavePixResponse
import br.com.zup.matheuscarv69.core.errorsHandler.ErrorHandler
import br.com.zup.matheuscarv69.pix.endpoints.registra.request.toModel
import br.com.zup.matheuscarv69.pix.endpoints.registra.service.NovaChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistrarChaveEndpoint(@Inject private val service: NovaChavePixService) :
    KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceImplBase() {

    override fun registrar(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

        val novaChave = request.toModel()
        val chaveCriada = service.registra(novaChave)

        responseObserver.onNext(
            RegistraChavePixResponse.newBuilder()
                .setPixId(chaveCriada.id.toString())
                .build()
        )

        responseObserver.onCompleted()
    }
}