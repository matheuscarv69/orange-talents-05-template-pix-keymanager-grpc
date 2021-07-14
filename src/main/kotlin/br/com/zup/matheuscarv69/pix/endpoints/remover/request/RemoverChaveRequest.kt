package br.com.zup.matheuscarv69.pix.endpoints.remover.request

import br.com.zup.matheuscarv69.core.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoverChaveRequest(
    @field:NotBlank
    @field:ValidUUID(message = "PixId com formato inválido")
    val pixId: String?,

    @field:NotBlank
    @field:ValidUUID(message = "ClienteId com formato inválido")
    val clienteId: String?
)
