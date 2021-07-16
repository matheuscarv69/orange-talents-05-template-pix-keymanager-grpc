package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.request

import br.com.zup.matheuscarv69.core.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Size

@Introspected
data class DetalharChaveRequest(
    @field:ValidUUID val pixId: String?,
    @field:ValidUUID val clienteId: String?,
    @field:Size(max = 77) val chave: String?
)