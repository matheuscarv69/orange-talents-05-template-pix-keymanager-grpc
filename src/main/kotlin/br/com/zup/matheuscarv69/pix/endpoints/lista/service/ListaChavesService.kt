package br.com.zup.matheuscarv69.pix.endpoints.lista.service

import br.com.zup.matheuscarv69.ListaChavesResponse
import br.com.zup.matheuscarv69.TipoDeChaveGrpc
import br.com.zup.matheuscarv69.TipoDeContaGrpc
import br.com.zup.matheuscarv69.pix.endpoints.lista.request.ListaRequest
import br.com.zup.matheuscarv69.pix.repositories.ChavePixRepository
import com.google.protobuf.Timestamp
import io.micronaut.validation.Validated
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class ListaChavesService(@Inject val repository: ChavePixRepository) {

    fun lista(@Valid request: ListaRequest): ListaChavesResponse {

        val chaves = repository.findAllByClienteId(UUID.fromString(request.clienteId)).map { chave ->
            ListaChavesResponse.ChaveResponse
                .newBuilder()
                .setPixId(chave.pixId)
                .setTipoDeChave(TipoDeChaveGrpc.valueOf(chave.tipoDeChave.name))
                .setChave(chave.chave)
                .setTipoDeConta(TipoDeContaGrpc.valueOf(chave.tipoDeConta.name))
                .setCriadaEm(chave.criadoEm.let { dataQuefoiCriada ->
                    val criadoEm = dataQuefoiCriada.atZone(ZoneId.of("UTC")).toInstant()

                    Timestamp.newBuilder()
                        .setSeconds(criadoEm.epochSecond)
                        .setNanos(criadoEm.nano)
                        .build()
                })
                .build()
        }

        return ListaChavesResponse.newBuilder()
            .setClienteId(request.clienteId)
            .addAllChaves(chaves)
            .build()
    }


}
