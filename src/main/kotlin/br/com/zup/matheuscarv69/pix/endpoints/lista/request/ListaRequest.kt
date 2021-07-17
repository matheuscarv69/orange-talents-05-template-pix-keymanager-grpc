package br.com.zup.matheuscarv69.pix.endpoints.lista.request

import br.com.zup.matheuscarv69.core.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected

@Introspected
data class ListaRequest(@field:ValidUUID val clienteId: String)