package br.com.zup.matheuscarv69.pix.repositories

import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {

    fun existsByPixId(pixId: String?): Boolean

    fun existsByChave(chave: String?): Boolean

    fun findByPixId(pixId: String?): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findByPixIdAndClienteId(pixId: String, clienteId: UUID): Optional<ChavePix>

    fun findAllByClienteId(clienteId: UUID): List<ChavePix>
}