package com.hartwig.actin.report.interpretation

import com.google.common.collect.Multimap
import com.hartwig.actin.molecular.datamodel.evidence.EligibleTrial
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence

class EvidenceInterpreter private constructor(private val actinInclusionEvents: Set<String>) {
    fun eventsWithApprovedEvidence(evidence: AggregatedEvidence): Set<String> {
        return evidence.approvedTreatmentsPerEvent().keySet()
    }

    fun additionalEventsWithExternalTrialEvidence(evidence: AggregatedEvidence): Set<String> {
        return filter2(evidence.externalEligibleTrialsPerEvent(), evidence)
    }

    fun additionalEventsWithOnLabelExperimentalEvidence(evidence: AggregatedEvidence): Set<String> {
        return filter(evidence.onLabelExperimentalTreatmentsPerEvent(), evidence)
    }

    fun additionalEventsWithOffLabelExperimentalEvidence(evidence: AggregatedEvidence): Set<String> {
        return filter(
            evidence.offLabelExperimentalTreatmentsPerEvent(),
            evidence,
            additionalEventsWithOnLabelExperimentalEvidence(evidence)
        )
    }

    private fun filter(
        eventMap: Multimap<String, String>, evidence: AggregatedEvidence, additionalEventsToFilter: Set<String> = emptySet()
    ): Set<String> {
        return eventMap.keySet() - eventsWithApprovedEvidence(evidence) - actinInclusionEvents - additionalEventsToFilter
    }

    private fun filter2(
        eventMap: Multimap<String, EligibleTrial>, evidence: AggregatedEvidence, additionalEventsToFilter: Set<String> = emptySet()
    ): Set<String> {
        return eventMap.keySet() - eventsWithApprovedEvidence(evidence) - actinInclusionEvents - additionalEventsToFilter
    }

    companion object {
        fun fromEvaluatedCohorts(cohorts: List<EvaluatedCohort>): EvidenceInterpreter {
            return EvidenceInterpreter(cohorts.flatMap(EvaluatedCohort::molecularEvents).toSet())
        }
    }
}