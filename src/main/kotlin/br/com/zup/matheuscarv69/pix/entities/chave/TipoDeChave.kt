package br.com.zup.matheuscarv69.pix.entities.chave

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDeChave {

    CPF {
        override fun valida(chave: String?): Boolean {

            if (chave.isNullOrBlank()) {
                return false;
            }

            if (!chave.matches("[0-9]+".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

        override fun converte() = this.name
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }

        override fun converte() = "PHONE"
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

        override fun converte() = this.name

    },
    ALEATORIA {
        override fun valida(chave: String?) = chave.isNullOrBlank()

        override fun converte() = "RANDOM"

    };

    abstract fun valida(chave: String?): Boolean

    abstract fun converte(): String
}