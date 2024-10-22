package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.Cohort
import com.hartwig.actin.report.pdf.util.Formats

data class ContentDefinition(val textEntries: List<String>, val deEmphasizeContent: Boolean)

object ActinTrialContentFunctions {

    fun contentForTrialCohortList(
        cohorts: List<Cohort>, feedbackFunction: (Cohort) -> Set<String>
    ): List<ContentDefinition> {
        val commonFeedback = findCommonMembersInCohorts(cohorts, feedbackFunction)
        val commonEvents = findCommonMembersInCohorts(cohorts, Cohort::molecularEvents)
        val allEventsEmpty = cohorts.all { it.molecularEvents.isEmpty() }
        val noFeedback = cohorts.all { feedbackFunction.invoke(it).isEmpty() }
        val prefix = if (commonFeedback.isEmpty() && commonEvents.isEmpty()) emptyList() else {
            val deEmphasizeContent = cohorts.all { !it.isOpen || !it.hasSlotsAvailable }
            listOf(
                ContentDefinition(
                    listOf(
                        "Applies to all cohorts below",
                        concat(commonEvents, allEventsEmpty && !noFeedback),
                        concat(commonFeedback, !noFeedback)
                    ), deEmphasizeContent
                )
            )
        }
        return prefix + cohorts.map { cohort: Cohort ->
            ContentDefinition(
                listOf(
                    cohort.cohort ?: "",
                    concat(cohort.molecularEvents - commonEvents, commonEvents.isEmpty() && !allEventsEmpty),
                    concat(feedbackFunction.invoke(cohort) - commonFeedback, commonFeedback.isEmpty() && !noFeedback)
                ),
                !cohort.isOpen || !cohort.hasSlotsAvailable
            )
        }
    }

    private fun findCommonMembersInCohorts(
        cohorts: List<Cohort>, retrieveMemberFunction: (Cohort) -> Set<String>
    ): Set<String> {
        return if (cohorts.size > 1) {
            cohorts.map(retrieveMemberFunction).reduce { acc, set -> acc.intersect(set) }
        } else emptySet()
    }

    private fun concat(strings: Set<String>, replaceEmptyWithNone: Boolean = true): String {
        val joinedString = strings.sorted().joinToString(Formats.COMMA_SEPARATOR)
        return if (replaceEmptyWithNone && joinedString.isEmpty()) Formats.VALUE_NONE else joinedString
    }
}