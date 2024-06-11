package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.pdf.util.Formats

data class ContentDefinition(val textEntries: List<String>, val deEmphasizeContent: Boolean)

object ActinTrialContentFunctions {

    fun contentForTrialCohortList(
        cohorts: List<EvaluatedCohort>, feedbackFunction: (EvaluatedCohort) -> Set<String>
    ): List<ContentDefinition> {
        val commonFeedback = if (cohorts.size > 1) {
            cohorts.map(feedbackFunction).reduce { acc, set -> acc.intersect(set) }
        } else emptySet()
        val prefix = if (commonFeedback.isEmpty()) emptyList() else {
            val deEmphasizeContent = cohorts.all { !it.isOpen || !it.hasSlotsAvailable }
            listOf(ContentDefinition(listOf("Applies to all cohorts below", "", concat(commonFeedback)), deEmphasizeContent))
        }
        return prefix + cohorts.map { cohort: EvaluatedCohort ->
            ContentDefinition(
                listOf(
                    cohort.cohort ?: "",
                    concat(cohort.molecularEvents, addNewlines = true),
                    concat(feedbackFunction.invoke(cohort) - commonFeedback, commonFeedback.isEmpty())
                ),
                !cohort.isOpen || !cohort.hasSlotsAvailable
            )
        }
    }

    private fun concat(strings: Set<String>, replaceEmptyWithNone: Boolean = true, addNewlines: Boolean = false): String {
        val joinedString = strings.sorted().joinToString(if (addNewlines) Formats.COMMA_SEPARATOR + "\n" else Formats.COMMA_SEPARATOR)
        return if (replaceEmptyWithNone && joinedString.isEmpty()) Formats.VALUE_NONE else joinedString
    }
}