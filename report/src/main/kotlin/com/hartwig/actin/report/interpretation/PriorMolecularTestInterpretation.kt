package com.hartwig.actin.report.interpretation

data class PriorMolecularTestInterpretation(
    val type: String,
    val results: List<PriorMolecularTestResultInterpretation>
)

data class PriorMolecularTestResultInterpretation(val grouping: String, val details: String)

class PriorMolecularTestInterpretationBuilder {
    private val results = mutableListOf<Triple<String, String, String>>()

    fun addInterpretation(type: String, grouping: String, details: String) {
        results.add(Triple(type, grouping, details))
    }

    fun build(): List<PriorMolecularTestInterpretation> {
        return results.groupBy { it.first }.map { (type, results) ->
            PriorMolecularTestInterpretation(type, results.map { PriorMolecularTestResultInterpretation(it.second, it.third) })
        }
    }
}