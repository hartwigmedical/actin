package com.hartwig.actin.report.interpretation

import org.apache.logging.log4j.util.Strings

object EvaluatedCohortTestFactory {

    fun evaluatedCohort(
        trialId: String = "",
        acronym: String = "",
        isPotentiallyEligible: Boolean = false,
        isOpen: Boolean = false,
        hasSlotsAvailable: Boolean = false,
        molecularEvents: Iterable<String> = emptySet(),
        cohort: String? = null
    ): EvaluatedCohort {
        return EvaluatedCohort(
            trialId = trialId,
            acronym = acronym,
            cohort = cohort,
            isPotentiallyEligible = isPotentiallyEligible,
            isOpen = isOpen,
            hasSlotsAvailable = hasSlotsAvailable,
            molecularEvents = molecularEvents.toSet(),
            warnings = emptySet(),
            fails = emptySet()
        )
    }

    fun builder(): ImmutableEvaluatedCohort.Builder {
        return ImmutableEvaluatedCohort.builder()
            .trialId(Strings.EMPTY)
            .acronym(Strings.EMPTY)
            .isPotentiallyEligible(false)
            .isOpen(false)
            .hasSlotsAvailable(false)
    }
}