package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFunctions
import com.hartwig.actin.report.pdf.util.Formats

data class ContentDefinition(val textEntries: List<String>, val deEmphasizeContent: Boolean)

object ActinTrialContentFunctions {

    fun contentForTrialCohortList(
        cohorts: List<InterpretedCohort>,
        feedbackFunction: (InterpretedCohort) -> Set<String>,
        includeFeedback: Boolean = true,
        requestingSource: TrialSource? = null
    ): List<ContentDefinition> {
        val commonFeedback = if (includeFeedback) findCommonMembersInCohorts(cohorts, feedbackFunction) else emptySet()
        val commonEvents = findCommonMembersInCohorts(cohorts, InterpretedCohort::molecularEvents)
        val commonLocations = findCommonMembersInCohorts(cohorts, InterpretedCohort::locations)
        val allEventsEmpty = cohorts.all { it.molecularEvents.isEmpty() }

        val hidePrefix = (commonFeedback.isEmpty() && commonEvents.isEmpty() && commonLocations.isEmpty()) || cohorts.size == 1

        val prefix = if (hidePrefix) emptyList() else {
            val deEmphasizeContent = cohorts.all { !it.isOpen || !it.hasSlotsAvailable }
            listOf(
                ContentDefinition(
                    listOfNotNull(
                        "${Formats.ITALIC_TEXT_MARKER}Applies to all cohorts below${Formats.ITALIC_TEXT_MARKER}",
                        concat(commonEvents, allEventsEmpty && includeFeedback),
                        concatLocations(cohorts.first().source, requestingSource, commonLocations),
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
                    concat(cohort.molecularEvents - commonEvents, commonEvents.isEmpty() && (!allEventsEmpty || hidePrefix)),
                    concatLocations(cohort.source, requestingSource, cohort.locations - commonLocations),
                    if (includeFeedback) concat(feedbackFunction(cohort) - commonFeedback, commonFeedback.isEmpty()) else null
                ),
                !cohort.isOpen || !cohort.hasSlotsAvailable
            )
        }
    }

    private fun concatLocations(source: TrialSource?, requestingSource: TrialSource?, locations: Set<String>): String {
        val showRequestingSite = requestingSource != null &&
                InterpretedCohortFunctions.sourceOrLocationMatchesRequestingSource(source, locations, requestingSource)

        return when {
            showRequestingSite && locations.size > MAX_TO_DISPLAY - 1 -> {
                val otherLocationCount = locations.size - 1
                "${requestingSource?.description} and $otherLocationCount other locations - see link"
            }

            locations.size > MAX_TO_DISPLAY -> MANY_SEE_LINK

            else -> concat(locations, false)
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