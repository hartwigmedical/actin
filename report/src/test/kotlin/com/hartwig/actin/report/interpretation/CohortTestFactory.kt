package com.hartwig.actin.report.interpretation

object CohortTestFactory {

    fun cohort(
        trialId: String = "",
        acronym: String = "",
        isPotentiallyEligible: Boolean = false,
        isOpen: Boolean = false,
        hasSlotsAvailable: Boolean = false,
        molecularEvents: Iterable<String> = emptySet(),
        cohort: String? = null
    ): Cohort {
        return Cohort(
            trialId = trialId,
            acronym = acronym,
            name = cohort,
            isPotentiallyEligible = isPotentiallyEligible,
            isMissingGenesForSufficientEvaluation = false,
            isOpen = isOpen,
            hasSlotsAvailable = hasSlotsAvailable,
            molecularEvents = molecularEvents.toSet(),
            warnings = emptySet(),
            fails = emptySet(),
            ignore = false
        )
    }
}