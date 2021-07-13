package br.com.zup.matheuscarv69.pix.registra

import br.com.zup.matheuscarv69.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.matheuscarv69.RegistraChavePixRequest
import br.com.zup.matheuscarv69.RegistraChavePixResponse
import br.com.zup.matheuscarv69.core.errors.grpc.ErrorHandler
import br.com.zup.matheuscarv69.pix.registra.request.toModel
import br.com.zup.matheuscarv69.pix.registra.service.NovaChavePixService
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
            RegistraChavePixResponse.newBuilder() // 1
                .setPixId(chaveCriada.id.toString())
                .build()
        )

        responseObserver.onCompleted()
    }
}