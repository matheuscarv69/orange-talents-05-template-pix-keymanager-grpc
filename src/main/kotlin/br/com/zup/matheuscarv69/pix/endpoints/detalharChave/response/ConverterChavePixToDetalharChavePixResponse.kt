package br.com.zup.matheuscarv69.pix.endpoints.detalharChave.response

import br.com.zup.matheuscarv69.DetalharChavePixResponse
import br.com.zup.matheuscarv69.TipoDeChaveGrpc
import br.com.zup.matheuscarv69.TipoDeContaGrpc
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConverterChavePixToDetalharChavePixResponse() {
    fun converter(chavePix: ChavePixResponse): DetalharChavePixResponse {
        return DetalharChavePixResponse.newBuilder()
            .setPixId(chavePix.pixId)
            .setClienteId(chavePix.clienteId.toString())
            .setChavePix(
                DetalharChavePixResponse.ChavePixResponse
                    .newBuilder()
                    .setTipoDeChave(TipoDeChaveGrpc.valueOf(chavePix.tipoDeChave.name))
                    .setChave(chavePix.chave)
                    .setConta(
                        DetalharChavePixResponse.ContaResponse.newBuilder()
                            .setTipo(TipoDeContaGrpc.valueOf(chavePix.tipoDeConta.name))
                            .setInstituicao(chavePix.conta.instituicao)
                            .setNomeDoTitular(chavePix.conta.nomeDoTitular)
                            .setCpfDoTitular(chavePix.conta.cpfDoTitular)
                            .setAgencia(chavePix.conta.agencia)
                            .setNumeroDaConta(chavePix.conta.numeroDaConta)
                            .build()
                    )
                    .setCriadaEm(chavePix.criadoEm.let { dataQuefoiCriada ->
                        val criadoEm = dataQuefoiCriada.atZone(ZoneId.of("UTC")).toInstant()

                        Timestamp.newBuilder()
                            .setSeconds(criadoEm.epochSecond)
                            .setNanos(criadoEm.nano)
                            .build()
                    })
                    .build()
            )
            .build()
    }
}