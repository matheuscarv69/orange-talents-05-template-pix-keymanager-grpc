package br.com.zup.matheuscarv69

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.matheuscarv69")
		.start()
}

