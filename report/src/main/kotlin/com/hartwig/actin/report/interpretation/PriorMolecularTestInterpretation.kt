package com.hartwig.actin.report.interpretation

data class PriorMolecularTestInterpretation(
    val type: String,
    val results: List<PriorMolecularTestResultInterpretation>
)

data class PriorMolecularTestResultInterpretation(val grouping: String, val details: String, val sortPrecedence: Int = 0)

class PriorMolecularTestInterpretationBuilder {

    private val results = mutableMapOf<Triple<String, String, String>, Int>()

    fun addInterpretation(type: String, grouping: String, details: String, sortPrecedence: Int = 0) {
        results[Triple(type, grouping, details)] = sortPrecedence
    }

    fun build(): List<PriorMolecularTestInterpretation> {
        return results.entries.groupBy { it.key.first }.map { (type, results) ->
            PriorMolecularTestInterpretation(
                type,
                results.map { PriorMolecularTestResultInterpretation(it.key.second, it.key.third, it.value) })
        }
    }
}