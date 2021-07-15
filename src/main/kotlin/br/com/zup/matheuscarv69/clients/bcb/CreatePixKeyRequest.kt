package br.com.zup.matheuscarv69.clients.bcb

import br.com.zup.matheuscarv69.pix.entities.chave.ChavePix
import br.com.zup.matheuscarv69.pix.entities.chave.ContaAssociada

data class CreatePixKeyRequest(val chavePix: ChavePix) {

    val keyType: String = chavePix.tipoDeChave.converte()
    val key: String = chavePix.chave
    val bankAccount: BankAccountRequest = BankAccountRequest(
        chavePix.conta!!,
        chavePix.tipoDeConta.converte()
    )
    val owner: OwnerRequest = OwnerRequest(chavePix.conta!!)

}


data class BankAccountRequest(
    val conta: ContaAssociada,
    val tipoDeConta: String
) {

    val participant: String = conta.ispb
    val branch: String = conta.agencia
    val accountNumber: String = conta.numeroDaConta
    val accountType: String = tipoDeConta

}

data class OwnerRequest(val conta: ContaAssociada) {

    val type: String = "NATURAL_PERSON"
    val name: String = conta.nomeDoTitular
    val taxIdNumber: String = conta.cpfDoTitular

}
