package com.hartwig.actin.report.interpretation

import java.time.LocalDate

data class PriorMolecularTestInterpretation(
    val type: String,
    val results: List<PriorMolecularTestResultInterpretation>
)

data class PriorMolecularTestResultInterpretation(
    val grouping: String, val details: String, val date: LocalDate?, val sortPrecedence: Int = 0
)

class PriorMolecularTestInterpretationBuilder {

    private data class PriorMolecularTestResult(val type: String, val grouping: String, val details: String, val date: LocalDate?)

    private val results = mutableMapOf<PriorMolecularTestResult, Int>()

    fun addInterpretation(type: String, grouping: String, details: String, date: LocalDate?, sortPrecedence: Int = 0) {
        results[PriorMolecularTestResult(type, grouping, details, date)] = sortPrecedence
    }

    fun build(): List<PriorMolecularTestInterpretation> {
        return results.entries.groupBy { it.key.type }.map { (type, results) ->
            PriorMolecularTestInterpretation(
                type,
                results.map { PriorMolecularTestResultInterpretation(it.key.grouping, it.key.details, it.key.date, it.value) })
        }
    }
}