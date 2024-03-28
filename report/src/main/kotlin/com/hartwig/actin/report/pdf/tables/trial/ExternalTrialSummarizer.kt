package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.EvaluatedCohort

data class ExternalTrialSummary(
    val dutchTrials: Map<String, Iterable<ExternalTrial>>,
    val otherCountryTrials: Map<String, Iterable<ExternalTrial>>
)

class ExternalTrialSummarizer(private val filterOverlappingMolecularTargets: Boolean) {

    fun summarize(
        externalEligibleTrials: Map<String, Iterable<ExternalTrial>>,
        hospitalLocalEvaluatedCohorts: List<EvaluatedCohort>
    ): ExternalTrialSummary {

        val hospitalTrialMolecularEvents = hospitalLocalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()
        val dutchTrials = filteredMolecularEvents(
            hospitalTrialMolecularEvents,
            EligibleExternalTrialGeneratorFunctions.dutchTrials(externalEligibleTrials)
        )
        val otherTrials = filteredMolecularEvents(
            hospitalTrialMolecularEvents + dutchTrials.keys,
            EligibleExternalTrialGeneratorFunctions.nonDutchTrials(externalEligibleTrials)
        )
        return ExternalTrialSummary(dutchTrials, otherTrials)
    }

    private fun filteredMolecularEvents(
        molecularTargetsAlreadyIncluded: Set<String>,
        trials: Map<String, Iterable<ExternalTrial>>
    ) = if (filterOverlappingMolecularTargets) trials.filter { !molecularTargetsAlreadyIncluded.contains(it.key) } else trials
}