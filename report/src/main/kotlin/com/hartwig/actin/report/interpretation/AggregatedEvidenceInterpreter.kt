package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

object AggregatedEvidenceInterpreter {

    fun filterExternalTrialsBasedOnNctId(
        externalTrialsPerEvent: Map<String, List<ExternalTrial>>, trialMatches: List<TrialMatch>
    ): Map<String, List<ExternalTrial>> {
        val localTrialNctIds = trialMatches.mapNotNull { it.identification.nctId }.toSet()
        return externalTrialsPerEvent
            .map { (event, trials) -> event to trials.filter { it.nctId !in localTrialNctIds } }
            .filter { it.second.isNotEmpty() }
            .toMap()
    }
}