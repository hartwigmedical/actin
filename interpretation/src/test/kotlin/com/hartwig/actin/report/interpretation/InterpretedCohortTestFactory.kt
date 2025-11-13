package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase

object InterpretedCohortTestFactory {

    fun interpretedCohort(
        trialId: String = "",
        acronym: String = "",
        nctId: String = "",
        title: String = "",
        phase: TrialPhase? = null,
        cohort: String? = null,
        locations: Set<String> = emptySet(),
        isOpen: Boolean = false,
        hasSlotsAvailable: Boolean = false,
        molecularInclusionEvents: Iterable<String> = emptySet(),
        isPotentiallyEligible: Boolean = false,
        isMissingMolecularResultForEvaluation: Boolean = false,
        isIgnore: Boolean = false,
        isEvaluable: Boolean = false,
    ): InterpretedCohort {
        return InterpretedCohort(
            trialId = trialId,
            acronym = acronym,
            nctId = nctId,
            title =  title,
            phase = phase,
            source = null,
            sourceId = null,
            locations = locations,
            url = null,
            name = cohort,
            isOpen = isOpen,
            hasSlotsAvailable = hasSlotsAvailable,
            ignore = isIgnore,
            isEvaluable = isEvaluable,
            molecularInclusionEvents = molecularInclusionEvents.toSet(),
            molecularExclusionEvents = emptySet(),
            isPotentiallyEligible = isPotentiallyEligible,
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation,
            warnings = emptySet(),
            fails = emptySet(),
        )
    }
}