package com.hartwig.actin.report.interpretation

object InterpretedCohortTestFactory {

    fun interpretedCohort(
        trialId: String = "",
        acronym: String = "",
        nctId: String = "",
        title: String = "",
        cohort: String? = null,
        locations: Set<String> = emptySet(),
        isOpen: Boolean = false,
        hasSlotsAvailable: Boolean = false,
        molecularEvents: Iterable<String> = emptySet(),
        isPotentiallyEligible: Boolean = false
    ): InterpretedCohort {
        return InterpretedCohort(
            trialId = trialId,
            acronym = acronym,
            nctId = nctId,
            title =  title,
            phase = null,
            source = null,
            sourceId = null,
            locations = locations,
            url = null,
            name = cohort,
            isOpen = isOpen,
            hasSlotsAvailable = hasSlotsAvailable,
            ignore = false,
            molecularEvents = molecularEvents.toSet(),
            isPotentiallyEligible = isPotentiallyEligible,
            isMissingMolecularResultForEvaluation = false,
            warnings = emptySet(),
            fails = emptySet(),

        )
    }
}