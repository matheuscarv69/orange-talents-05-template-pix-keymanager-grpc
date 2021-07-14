package br.com.zup.matheuscarv69.pix.entities.chave

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveTest {

    @Nested
    inner class CPF {

        @Test
        fun `Deve ser valido quando o CPF informado for um numero valido`() {
            with(TipoDeChave.CPF) {
                assertTrue(valida("87128525033"))
            }
        }

        @Test
        fun `Deve ser invalido quando o CPF informado for numero invalido`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida("12345678910"))
            }
        }

        @Test
        fun `Nao deve ser valido quando o CPF informado conter letras ou caracteres invalidos`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida("871a85@/033"))
            }
        }

        @Test
        fun `Nao deve ser valido quando o CPF nao for informado`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }

    }

    @Nested
    inner class CELULAR {

        @Test
        fun `Deve ser valido quando o Celular informado tiver o formato correto`() {
            with(TipoDeChave.CELULAR) {
                assertTrue(valida("+55959981187680"))
            }
        }

        @Test
        fun `Nao deve ser valido quando o Celular informado tiver o formato incorreto`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida("959981187680"))
            }
        }

        @Test
        fun `Nao deve ser valido quando o Celular nao for informado`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `Deve ser valido quando o Email for valido`() {
            with(TipoDeChave.EMAIL) {
                assertTrue(valida("rafa.ponte@zup.com.br"))
            }
        }

        @Test
        fun `Nao deve ser valido quando o Email informado for invalido`() {
            with(TipoDeChave.EMAIL) {
                assertFalse(valida("yuri.matheus.zup.com.br"))
                assertFalse(valida("yuri.matheus@zup.com."))
            }
        }

        @Test
        fun `Nao deve ser valido quando o Email nao for informado`() {
            with(TipoDeChave.EMAIL) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class ALEATORIA {

        @Test
        fun `Deve ser valido quando a chave pix estiver vazia`() {
            with(TipoDeChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `Nao deve ser valido quando a chave pix estiver preenchida`() {
            with(TipoDeChave.ALEATORIA) {
                assertFalse(valida("estou cansado ='( "))
            }
        }

    }


}