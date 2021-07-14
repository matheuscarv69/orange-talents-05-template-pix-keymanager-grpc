package br.com.zup.matheuscarv69.core.errorsHandler.handlers

import br.com.zup.matheuscarv69.core.errorsHandler.ExceptionHandler
import br.com.zup.matheuscarv69.core.errorsHandler.ExceptionHandler.StatusWithDetails
import br.com.zup.matheuscarv69.core.errorsHandler.exceptions.ChavePixNaoPertenceAoClienteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoPertenceAoClienteExceptionHandler : ExceptionHandler<ChavePixNaoPertenceAoClienteException> {

    override fun handle(e: ChavePixNaoPertenceAoClienteException): StatusWithDetails {
        return StatusWithDetails(
            Status.FAILED_PRECONDITION
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoPertenceAoClienteException
    }

}