package com.hartwig.actin.report.interpretation

import java.time.LocalDate

data class IHCTestInterpretation(
    val type: String,
    val results: List<IHCTestResultInterpretation>
)

data class IHCTestResultInterpretation(
    val grouping: String, val details: String, val date: LocalDate?, val sortPrecedence: Int = 0
)

class IHCTestInterpretationBuilder {

    private data class IHCTestResult(val type: String, val grouping: String, val details: String, val date: LocalDate?)

    private val results = mutableMapOf<IHCTestResult, Int>()

    fun addInterpretation(type: String, grouping: String, details: String, date: LocalDate?, sortPrecedence: Int = 0) {
        results[IHCTestResult(type, grouping, details, date)] = sortPrecedence
    }

    fun build(): List<IHCTestInterpretation> {
        return results.entries.groupBy { it.key.type }.map { (type, results) ->
            IHCTestInterpretation(
                type,
                results.map { IHCTestResultInterpretation(it.key.grouping, it.key.details, it.key.date, it.value) })
        }
    }
}