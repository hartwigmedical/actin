package com.hartwig.actin.report.interpretation

import java.time.LocalDate

data class IhcTestInterpretation(
    val type: String,
    val results: List<IhcTestResultInterpretation>
)

data class IhcTestResultInterpretation(
    val grouping: String, val details: String, val date: LocalDate?, val sortPrecedence: Int = 0
)

class IhcTestInterpretationBuilder {

    private data class IhcTestResult(val type: String, val grouping: String, val details: String, val date: LocalDate?)

    private val results = mutableMapOf<IhcTestResult, Int>()

    fun addInterpretation(type: String, grouping: String, details: String, date: LocalDate?, sortPrecedence: Int = 0) {
        results[IhcTestResult(type, grouping, details, date)] = sortPrecedence
    }

    fun build(): List<IhcTestInterpretation> {
        return results.entries.groupBy { it.key.type }.map { (type, results) ->
            IhcTestInterpretation(
                type,
                results.map { IhcTestResultInterpretation(it.key.grouping, it.key.details, it.key.date, it.value) })
        }
    }
}