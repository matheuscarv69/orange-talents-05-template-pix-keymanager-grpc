package br.com.zup.matheuscarv69.clients.bcb

import java.time.LocalDateTime

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
) {

}
