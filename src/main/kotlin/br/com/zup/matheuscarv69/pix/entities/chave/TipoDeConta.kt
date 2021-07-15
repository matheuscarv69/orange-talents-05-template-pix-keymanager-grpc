package br.com.zup.matheuscarv69.pix.entities.chave

enum class TipoDeConta {

    CONTA_CORRENTE {
        override fun converte() = "CACC"
    },
    CONTA_POUPANCA {
        override fun converte() = "SVGS"
    };


    abstract fun converte(): String

}
