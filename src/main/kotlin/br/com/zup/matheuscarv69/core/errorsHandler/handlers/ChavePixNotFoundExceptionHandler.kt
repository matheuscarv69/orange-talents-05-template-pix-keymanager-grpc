package br.com.zup.matheuscarv69.core.errorsHandler.handlers

import br.com.zup.matheuscarv69.core.errorsHandler.ExceptionHandler
import br.com.zup.matheuscarv69.core.errorsHandler.ExceptionHandler.StatusWithDetails
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixNotFoundException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNotFoundExceptionHandler : ExceptionHandler<ChavePixNotFoundException> {

    override fun handle(e: ChavePixNotFoundException): StatusWithDetails {
        return StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNotFoundException
    }

}