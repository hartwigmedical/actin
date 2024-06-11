package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.EvaluatedCohort

data class ExternalTrialSummary(
    val dutchTrials: Map<String, Iterable<ExternalTrial>>,
    val dutchTrialsFiltered: Int,
    val otherCountryTrials: Map<String, Iterable<ExternalTrial>>,
    val otherCountryTrialsFiltered: Int
)

class ExternalTrialSummarizer {

    fun summarize(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, trialMatches: List<TrialMatch>, evaluatedCohorts: List<EvaluatedCohort>): ExternalTrialSummary {
        return filterMolecularCriteriaAlreadyPresent(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches), evaluatedCohorts)
    }

    fun filterAndGroupExternalTrialsByNctIdAndEvents(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, trialMatches: List<TrialMatch>
    ): Map<String, List<ExternalTrial>> {
        val localTrialNctIds = trialMatches.mapNotNull { it.identification.nctId }.toSet()
        return externalTrialsPerEvent.flatMap { (event, trials) -> trials.filter { it.nctId !in localTrialNctIds }.map { event to it } }
            .groupBy { (_, trial) -> trial.nctId }
            .map { (_, eventAndTrialPairs) ->
                val (events, trials) = eventAndTrialPairs.unzip()
                events.joinToString(",\n") to trials.first()
            }
            .groupBy({ it.first }, { it.second })
    }

    fun filterMolecularCriteriaAlreadyPresent(
        externalEligibleTrials: Map<String, Iterable<ExternalTrial>>,
        hospitalLocalEvaluatedCohorts: List<EvaluatedCohort>
    ): ExternalTrialSummary {

        val hospitalTrialMolecularEvents = hospitalLocalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()
        val (dutchTrials, dutchTrialsFiltered) = filteredMolecularEvents(
            hospitalTrialMolecularEvents,
            EligibleExternalTrialGeneratorFunctions.dutchTrials(externalEligibleTrials)
        )
        val (otherTrials, otherTrialsFiltered) = filteredMolecularEvents(
            hospitalTrialMolecularEvents + dutchTrials.keys.flatMap { splitMolecularEvents(it) },
            EligibleExternalTrialGeneratorFunctions.nonDutchTrials(externalEligibleTrials)
        )
        return ExternalTrialSummary(dutchTrials, dutchTrialsFiltered, otherTrials, otherTrialsFiltered)
    }

    private fun filteredMolecularEvents(
        molecularTargetsAlreadyIncluded: Set<String>,
        trials: Map<String, Iterable<ExternalTrial>>
    ): Pair<Map<String, Iterable<ExternalTrial>>, Int> {
        val filtered = trials.filterNot {
            splitMolecularEvents(it.key).all { mt -> molecularTargetsAlreadyIncluded.contains(mt) }
        }
        return filtered to uniqueTrialCount(trials) - uniqueTrialCount(filtered)
    }

    private fun uniqueTrialCount(trials: Map<String, Iterable<ExternalTrial>>) =
        trials.flatMap { it.value }.toSet().size

    private fun splitMolecularEvents(molecularEvents: String) =
        molecularEvents.split(",").map { it.trim() }.toSet()
}