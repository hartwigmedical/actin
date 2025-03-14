package com.hartwig.actin.report.interpretation

object InterpretedCohortTestFactory {

    fun interpretedCohort(
        trialId: String = "",
        acronym: String = "",
        nctId: String = "",
        title: String = "",
        isPotentiallyEligible: Boolean = false,
        isOpen: Boolean = false,
        hasSlotsAvailable: Boolean = false,
        molecularEvents: Iterable<String> = emptySet(),
        cohort: String? = null,
        locations: List<String> = emptyList()
    ): InterpretedCohort {
        return InterpretedCohort(
            trialId = trialId,
            acronym = acronym,
            nctId = nctId,
            title =  title,
            name = cohort,
            isPotentiallyEligible = isPotentiallyEligible,
            isMissingMolecularResultForEvaluation = false,
            isOpen = isOpen,
            hasSlotsAvailable = hasSlotsAvailable,
            molecularEvents = molecularEvents.toSet(),
            warnings = emptySet(),
            fails = emptySet(),
            ignore = false,
            locations = locations
        )
    }
}