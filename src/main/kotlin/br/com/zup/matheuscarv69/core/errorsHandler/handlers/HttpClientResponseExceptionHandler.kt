package br.com.zup.matheuscarv69.core.errorsHandler.handlers

import br.com.zup.matheuscarv69.core.errorsHandler.ExceptionHandler
import io.grpc.Status
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class HttpClientResponseExceptionHandler : ExceptionHandler<HttpClientResponseException> {

    override fun handle(e: HttpClientResponseException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.FAILED_PRECONDITION
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is HttpClientResponseException
    }
}