package com.javasampleapproach.kotlin.csv

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList

private val CIDADE_REGIAO = 1
private val CIDADE_NOME = 4
private val CIDADE_TOTAL = 22

fun main(args: Array<String>?) {
	var fileReader: BufferedReader? = null

	try {
		val cidades = ArrayList<Cidade>()
		val semanas = ArrayList<Semana>()
		val cidadeSemanas = ArrayList<CidadeSemana>()
		var line: String?

		for (i in 1..17) {
			val numeroSemana: String?
			if (i < 10) {
				numeroSemana = "0" + i
			} else {
				numeroSemana = i.toString()
			}

			val semana = Semana(
				i,
				"SE" + numeroSemana,
				i + 4
			)
			semanas.add(semana)
		}
		fileReader =
			BufferedReader(FileReader("municipios-semanas.csv"))

		// Read CSV header
		fileReader.readLine()

		// Read the file line by line starting from the second line
		line = fileReader.readLine()
		while (line != null) {
			val tokens = line.split(",")
			if (tokens.size > 0) {
				val municipio = tokens[CIDADE_NOME];
				val idCidade = municipio.substring(0, 6)
				val nomeCidade = municipio.substring(7)

				val cidade = Cidade(
					Integer.parseInt(idCidade),
					nomeCidade,
					Integer.parseInt(tokens[CIDADE_TOTAL]),
					tokens[CIDADE_REGIAO]
				)
				var temCasosDengue = false
				for (sem in semanas) {
					val numeroCasos = Integer.parseInt(tokens[sem.index])
					if (numeroCasos > 10) {
						val cidadeSemana = CidadeSemana(
							cidade.id,
							sem.id,
							numeroCasos
						)
						if (!temCasosDengue) {
							temCasosDengue = true
						}
						cidadeSemanas.add(cidadeSemana)
					}
				}
				if (temCasosDengue) {
					cidades.add(cidade)
				}

			}
			line = fileReader.readLine()
		}

		escreveArquivoNos(cidades, semanas)
		escreveArquivoArestas(cidadeSemanas)

		// Print the new customer list
/*		for (cidade in cidades) {
			println(cidade)
		}
*/
	} catch (e: Exception) {
		println("Reading CSV Error!")
		e.printStackTrace()
	} finally {
		try {
			fileReader!!.close()
		} catch (e: IOException) {
			println("Closing fileReader Error!")
			e.printStackTrace()
		}
	}
}

fun escreveArquivoNos(cidades: List<Cidade>, semanas: List<Semana>) {
	println("Escrevendo nodes: ${cidades.size + semanas.size}")
	val writer =
		File("nodes-cidades-semanas.csv").printWriter()
	writer.write("ID,LABEL,TIPO, REGIAO, TOTALCASOS\n")
	for (cidade in cidades) {
		writer.write("${cidade.id},${cidade.nome}, CIDADE, ${cidade.regiao}, ${cidade.totalCasos}\n")
	}
	for (semana in semanas) {
		writer.write("${semana.id},${semana.label}, SEMANA, , \n")
	}
	writer.close()
}


fun escreveArquivoArestas(cidadesSemanas: List<CidadeSemana>) {
	println("Escrevendo arestas: ${cidadesSemanas.size}")
	val writer =
		File("edges-cidades-semanas.csv").printWriter()
	writer.write("SOURCE,TARGET,WEIGHT\n")
	for (cidadeSemana in cidadesSemanas) {
		writer.write("${cidadeSemana.idCidade},${cidadeSemana.idSemana},${cidadeSemana.numeroCasos}\n")
	}
	writer.close()
}


data class Cidade(
	val id: Int,
	val nome: String,
	val totalCasos: Int,
	val regiao: String
)

data class Semana(
	val id: Int,
	val label: String,
	val index: Int
)

data class CidadeSemana(
	val idCidade: Int,
	val idSemana: Int,
	val numeroCasos: Int
)
