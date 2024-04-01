package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.EvaluatedCohort

data class ExternalTrialSummary(
    val dutchTrials: Map<String, Iterable<ExternalTrial>>,
    val dutchTrialsFiltered: Int,
    val otherCountryTrials: Map<String, Iterable<ExternalTrial>>,
    val otherCountryTrialsFiltered: Int
)

class ExternalTrialSummarizer(private val filterOverlappingMolecularTargets: Boolean) {

    fun summarize(
        externalEligibleTrials: Map<String, Iterable<ExternalTrial>>,
        hospitalLocalEvaluatedCohorts: List<EvaluatedCohort>
    ): ExternalTrialSummary {

        val hospitalTrialMolecularEvents = hospitalLocalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()
        val (dutchTrials, dutchTrialsFiltered) = filteredMolecularEvents(
            hospitalTrialMolecularEvents,
            EligibleExternalTrialGeneratorFunctions.dutchTrials(externalEligibleTrials)
        )
        val (otherTrials, otherTrialsFiltered) = filteredMolecularEvents(
            hospitalTrialMolecularEvents + dutchTrials.keys,
            EligibleExternalTrialGeneratorFunctions.nonDutchTrials(externalEligibleTrials)
        )
        return ExternalTrialSummary(dutchTrials, dutchTrialsFiltered, otherTrials, otherTrialsFiltered)
    }

    private fun filteredMolecularEvents(
        molecularTargetsAlreadyIncluded: Set<String>,
        trials: Map<String, Iterable<ExternalTrial>>
    ): Pair<Map<String, Iterable<ExternalTrial>>, Int> {
        val filtered =
            if (filterOverlappingMolecularTargets) trials.filter { !molecularTargetsAlreadyIncluded.contains(it.key) } else trials
        return filtered to uniqueTrialCount(trials) - uniqueTrialCount(filtered)
    }

    private fun uniqueTrialCount(trials: Map<String, Iterable<ExternalTrial>>) =
        trials.flatMap { it.value }.toSet().size
}