package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.service

import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.request.DetalharChaveRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

class ValidaDetalharRequest {

    companion object {

        fun validaRequest(request: DetalharChaveRequest, validator: Validator): Boolean {
            val detalharPorPixId = request.pixId!!.isNotEmpty() && request.clienteId!!.isNotEmpty()

            return if (detalharPorPixId) {
                val violations = validator.validate(request)
                if (violations.isNotEmpty())
                    throw ConstraintViolationException(violations)
                detalharPorPixId
            } else {
                val violations = validator.validate(request.chave!!)
                if (violations.isNotEmpty())
                    throw ConstraintViolationException(violations)
                if (request.chave.isEmpty())
                    throw IllegalStateException("Dados inválidos, informe o PixId e o ClienteId ou a Chave")
                detalharPorPixId
            }
        }

    }


}