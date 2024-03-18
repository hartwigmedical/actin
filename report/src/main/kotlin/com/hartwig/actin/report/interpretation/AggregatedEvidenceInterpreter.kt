package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

object AggregatedEvidenceInterpreter {

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
}