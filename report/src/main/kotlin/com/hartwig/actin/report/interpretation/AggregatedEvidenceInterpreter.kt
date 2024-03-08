package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence

class AggregatedEvidenceInterpreter {

    fun filterExternalTrialsBasedOnNctId(
        aggregatedEvidence: AggregatedEvidence, treatmentMatch: TreatmentMatch
    ): Map<String, List<ExternalTrial>> {
        val localTrialNctIds = treatmentMatch.trialMatches.mapNotNull { it.identification.nctId }.toSet()
        return aggregatedEvidence.externalEligibleTrialsPerEvent
            .map { (event, trials) -> event to trials.filter { it.nctId !in localTrialNctIds } }
            .filter { it.second.isNotEmpty() }
            .toMap()
    }
}