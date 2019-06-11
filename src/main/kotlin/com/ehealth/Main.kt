package com.ehealth

import java.io.File
import java.lang.IllegalStateException
import java.util.concurrent.ThreadLocalRandom

// população levantada em maio2019
// qtd de casos de dengue entre jan2019 e abril2019
val BAURU = Cidade("Bauru", 337000, 13600)
val ARARAQUARA = Cidade("Araraquara", 143000, 8000)
val RIO_PRETO = Cidade("Rio Preto", 372000, 8000)
val CAMPINAS = Cidade("Campinas", 1000000, 5500)
val ANDRADINA = Cidade("Andradina", 55000, 3400)
val BARRETOS = Cidade("Barretos", 119000, 3200)
val SAO_JOAQUIM_DA_BARRA = Cidade("São Joaquim da Barra", 51000, 2700)
val SAO_PAULO = Cidade("São Paulo", 12000000, 3100)
val BIRIGUI = Cidade("Birigui", 105000, 2500)
val FERNANDOPOLIS = Cidade("Fernandópolis", 68000, 2200)

val CIDADES = listOf<Cidade>(BAURU, ARARAQUARA, RIO_PRETO, CAMPINAS, ANDRADINA, BARRETOS,
        SAO_JOAQUIM_DA_BARRA, SAO_PAULO, BIRIGUI, FERNANDOPOLIS)
val CIDADES_POR_POPULACAO = CIDADES.sortedBy { it.populacao }

val TOTAL_POPULACAO = CIDADES.sumBy { it.populacao }
val TOTAL_CASOS_DENGUE = CIDADES.sumBy { it.qtdCasosDengue }

val QTD_AMOSTRA_POPULACAO = 100000

// Estamos considerando que uma pessoa encontra 1000 pessoas da mesma cidade, e 100 pessoas de cidade distinta
val PROBABILIDADE_ENCONTRO_MESMA_CIDADE = 0.001       // 100 / 100.000
val PROBABILIDADE_ENCONTRO_CIDADES_DISTINTAS = 0.0001 // 10  / 100.000

fun main(args : Array<String>) {
    println("Iremos  criar $QTD_AMOSTRA_POPULACAO pessoas")
    val pessoas = mutableListOf<Pessoa>()

    // gerando população de pessoas
    for (i in 1..QTD_AMOSTRA_POPULACAO) {
        val cidade = sorteiaCidade()
        val dengue = sorteiaDengue(cidade)
        pessoas.add(Pessoa(i, cidade, dengue, calculaProbabilidadeDengue(cidade, dengue)))
    }

    // relatorio com quantidade de pessoas com dengue
    printaRelatorioCasosDengue(pessoas)
    escreveArquivoNos(pessoas)


    // filtrando so pessoas com dengue
    val pessoasComDengue = pessoas.filter { it.dengueNosUltimos12Meses }

    val encontrosAlvo = mutableListOf<EncontroDengue>()

    for (p1 in pessoas) {
        for (p2 in pessoasComDengue) {
            if (p1.id < p2.id) {
                // p1 pode ter dengue ou não. P2 tem dengue
                if (sorteiaEncontro(p1.cidade, p2.cidade)) {
		    val res = CIDADES.filter {cidade -> cidade.nome == p1.cidade.nome}
                    encontrosAlvo.add(EncontroDengue(p1.id, p2.id, res.get(0).probDengue))
                }
            }
        }
    }

    // quantidade de encontros
    println(encontrosAlvo.size)

    escreveArquivoArestas(encontrosAlvo)

    println("Hello, World!")
}


fun sorteiaCidade(): Cidade {
    val randomNumber = ThreadLocalRandom.current().nextInt(1, TOTAL_POPULACAO)
    var currentSum = 0
    for (cidade in CIDADES_POR_POPULACAO) {
        currentSum += cidade.populacao
        if (currentSum > randomNumber) {
            return cidade
        }
    }

    throw IllegalStateException("Deveria ter escolhido uma cidade a partir do id random: $randomNumber")
}

fun sorteiaDengue(cidade: Cidade): Boolean {
    val randomNumber = ThreadLocalRandom.current().nextDouble(0.0, 1.0)
    return randomNumber <= cidade.probDengue
}

fun calculaProbabilidadeDengue(cidade: Cidade, dengue: Boolean): Double {
    if (dengue) {
	return 1.0
    } else {
	return cidade.probDengue
    }
}

fun sorteiaEncontro(cidade1: Cidade, cidade2: Cidade): Boolean {
    val randomNumber = ThreadLocalRandom.current().nextDouble(0.0, 1.0)
    if (cidade1.nome == cidade2.nome) {
        return randomNumber <= PROBABILIDADE_ENCONTRO_MESMA_CIDADE
    } else {
        return randomNumber <= PROBABILIDADE_ENCONTRO_CIDADES_DISTINTAS
    }
}

fun printaRelatorioCasosDengue(pessoas: List<Pessoa>) {
    val denguePorCidade = mutableMapOf<String, Int>()
    val populacaoPorCidade = mutableMapOf<String, Int>()
    for (cidade in CIDADES) {
        denguePorCidade[cidade.nome] = 0
        populacaoPorCidade[cidade.nome] = 0
    }

    for (pessoa in pessoas) {
        populacaoPorCidade[pessoa.cidade.nome] = populacaoPorCidade[pessoa.cidade.nome]!!.inc()
        if (pessoa.dengueNosUltimos12Meses) {
            denguePorCidade[pessoa.cidade.nome]= denguePorCidade[pessoa.cidade.nome]!!.inc()
        }
    }

    for (cidade in CIDADES) {
        println("${cidade.nome}: População: ${populacaoPorCidade[cidade.nome]}." +
                " Casos Dengue: ${denguePorCidade[cidade.nome]}")
    }
}

fun escreveArquivoArestas(encontrosAlvo: List<EncontroDengue>) {
    println("Escrevendo arestas: ${encontrosAlvo.size}")
    val writer = File("edges.csv").printWriter()
    writer.write("SOURCE,TARGET,WEIGHT\n")
    for (encontro in encontrosAlvo) {
        writer.write("${encontro.pessoa2},${encontro.pessoa1},${encontro.weight}\n")
    }
    writer.close()
}

fun escreveArquivoNos(pessoas: List<Pessoa>) {
    println("Escrevendo nodes: ${pessoas.size}")
    val writer = File("nodes.csv").printWriter()
    writer.write("ID,CIDADE,DENGUE,WEIGHT\n")
    for (pessoa in pessoas) {
        writer.write("${pessoa.id},${pessoa.cidade.nome}, ${pessoa.dengueNosUltimos12Meses}, ${pessoa.probDengue}\n")
    }
    writer.close()
}

data class Pessoa(val id: Int,
                  val cidade: Cidade,
                  val dengueNosUltimos12Meses: Boolean,
		  val probDengue: Double)

data class Cidade(val nome: String,
                  val populacao: Int,
                  val qtdCasosDengue: Int,
                  val probDengue: Double = qtdCasosDengue.toDouble() / populacao)

class EncontroDengue(val pessoa1: Int,
                     val pessoa2: Int,
		     val weight: Double)
