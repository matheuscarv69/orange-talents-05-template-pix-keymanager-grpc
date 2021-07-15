package br.com.zup.matheuscarv69.pix.entities.chave

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "uk_chave_pix",
        columnNames = ["chave"]
    )]
)
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeChave: TipoDeChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeConta: TipoDeConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @field:NotBlank
    @Column(nullable = false, unique = true)
    val pixId: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()

    fun atualizaChave(chave: String): Boolean {
        if (isAleatoria()) {
            this.chave = chave
            return true
        }
        return false
    }

    fun isAleatoria(): Boolean {
        return this.tipoDeChave == TipoDeChave.ALEATORIA
    }

    fun pertenceAoCliente(clienteId: String?): Boolean {
        return this.clienteId == UUID.fromString(clienteId)
    }

}