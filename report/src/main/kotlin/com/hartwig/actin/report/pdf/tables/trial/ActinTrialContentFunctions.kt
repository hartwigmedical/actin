package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.util.Formats

data class ContentDefinition(val textEntries: List<String>, val deEmphasizeContent: Boolean)

object ActinTrialContentFunctions {

    fun contentForTrialCohortList(
        cohorts: List<InterpretedCohort>,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeFeedback: Boolean = true
    ): List<ContentDefinition> {
        val commonFeedback = if (includeFeedback) findCommonMembersInCohorts(cohorts, feedbackFunction) else emptySet()
        val commonEvents = findCommonMembersInCohorts(cohorts, InterpretedCohort::molecularEvents)
        val commonLocations = findCommonMembersInCohorts(cohorts, InterpretedCohort::locations)
        val allEventsEmpty = cohorts.all { it.molecularEvents.isEmpty() }

        val hidePrefix = commonFeedback.isEmpty() && commonEvents.isEmpty() && commonLocations.isEmpty()

        val prefix = if (hidePrefix) emptyList() else {
            val deEmphasizeContent = cohorts.all { !it.isOpen || !it.hasSlotsAvailable }
            listOf(
                ContentDefinition(
                    listOfNotNull(
                        "Applies to all cohorts below",
                        concat(commonEvents, allEventsEmpty && includeFeedback),
                        concat(commonLocations, false),
                        concat(commonFeedback).takeIf { includeFeedback }
                    ),
                    deEmphasizeContent
                )
            )
        }

        return prefix + cohorts.map { cohort: InterpretedCohort ->
            ContentDefinition(
                listOfNotNull(
                    cohort.name ?: "",
                    concat(cohort.molecularEvents - commonEvents, commonEvents.isEmpty() && !allEventsEmpty),
                    concat(cohort.locations - commonLocations, false),
                    if (includeFeedback) concat(feedbackFunction(cohort) - commonFeedback, commonFeedback.isEmpty()) else null
                ),
                !cohort.isOpen || !cohort.hasSlotsAvailable
            )
        }
    }

    private fun findCommonMembersInCohorts(
        cohorts: List<InterpretedCohort>, retrieveMemberFunction: (InterpretedCohort) -> Set<String>
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