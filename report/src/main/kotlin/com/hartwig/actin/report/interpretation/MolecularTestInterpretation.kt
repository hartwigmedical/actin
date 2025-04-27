package com.hartwig.actin.report.interpretation

import java.time.LocalDate

data class MolecularTestInterpretation(
    val type: String,
    val results: List<MolecularTestResultInterpretation>
)

data class MolecularTestResultInterpretation(
    val grouping: String, val details: String, val date: LocalDate?, val sortPrecedence: Int = 0
)

class MolecularTestInterpretationBuilder {

    private data class MolecularTestResult(val type: String, val grouping: String, val details: String, val date: LocalDate?)

    private val results = mutableMapOf<MolecularTestResult, Int>()

    fun addInterpretation(type: String, grouping: String, details: String, date: LocalDate?, sortPrecedence: Int = 0) {
        results[MolecularTestResult(type, grouping, details, date)] = sortPrecedence
    }

    fun build(): List<MolecularTestInterpretation> {
        return results.entries.groupBy { it.key.type }.map { (type, results) ->
            MolecularTestInterpretation(
                type,
                results.map { MolecularTestResultInterpretation(it.key.grouping, it.key.details, it.key.date, it.value) })
        }
    }
}