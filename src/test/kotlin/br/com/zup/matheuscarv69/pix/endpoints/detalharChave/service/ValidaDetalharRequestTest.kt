package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.service

import br.com.zup.matheuscarv69.pix.endpoints.detalharChave.request.DetalharChaveRequest
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@MicronautTest
internal class ValidaDetalharRequestTest(@Inject val validator: Validator) {

    /**
     * Testes unitários???
     * 1. Nao deve validar quando o pixId e o clientId forem diferentes de UUID
     * 2. Nao deve validar quando a chave for maior que 77 caracteres
     * 3. Nao deve validar quando os dados forem invalidos
     */

    @Test
    fun `Nao deve validar quando o pixId e o clientId forem diferentes de UUID`() {
        // cenario
        val pixId = "isso eh um pix id em! confia"
        val clienteId = "isso eh um cliente id em! confia"

        val request = DetalharChaveRequest(pixId = pixId, clienteId = clienteId, chave = null)

        // acao
        val errors = assertThrows<ConstraintViolationException> {
            ValidaDetalharRequest.validaRequest(request, validator)
        }

        // validacao
        with(errors) {
            assertNotNull(this)

            // Gambi, jah sao 20:31 de uma sexta, vou tratar da maneira certa logo logo
            val constraints: Array<String> = arrayOf(
                """pixId: must match "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", clienteId: must match "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"""",
                """clienteId: must match "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$, pixId: must match "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}${'$'}""""
            )
            assertTrue(constraints.contains(this.message))
        }
    }
}