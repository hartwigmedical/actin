package com.hartwig.actin.report.interpretation

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
            isMissingGenesForSufficientEvaluation = false,
            isOpen = isOpen,
            hasSlotsAvailable = hasSlotsAvailable,
            molecularEvents = molecularEvents.toSet(),
            warnings = emptySet(),
            fails = emptySet()
        )
    }
}