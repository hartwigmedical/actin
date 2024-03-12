package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

object AggregatedEvidenceInterpreter {

    fun groupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent: Map<String, List<ExternalTrial>>): Map<String, List<ExternalTrial>> {
        return externalTrialsPerEvent.flatMap { (event, trials) -> trials.map { event to it } }
            .groupBy{ (_, trial) -> trial.nctId }
            .map { (_, eventAndTrialPairs) ->
                val (events, trials) = eventAndTrialPairs.unzip()
                events.joinToString(",\n") to trials.first()
            }
            .groupBy({ it.first }, { it.second })
    }
}