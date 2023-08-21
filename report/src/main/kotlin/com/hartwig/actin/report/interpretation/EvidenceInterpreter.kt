package com.hartwig.actin.report.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence

class EvidenceInterpreter private constructor(private val actinInclusionEvents: Set<String?>) {
    fun eventsWithApprovedEvidence(evidence: AggregatedEvidence): Set<String?> {
        return evidence.approvedTreatmentsPerEvent().keySet()
    }

    fun additionalEventsWithExternalTrialEvidence(evidence: AggregatedEvidence): Set<String?> {
        val eventsToFilter: MutableSet<String?> = Sets.newHashSet()
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence))
        eventsToFilter.addAll(actinInclusionEvents)
        val events = evidence.externalEligibleTrialsPerEvent().keySet()
        return filter(events, eventsToFilter)
    }

    fun additionalEventsWithOnLabelExperimentalEvidence(evidence: AggregatedEvidence): Set<String?> {
        val eventsToFilter: MutableSet<String?> = Sets.newHashSet()
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence))
        eventsToFilter.addAll(actinInclusionEvents)
        val events = evidence.onLabelExperimentalTreatmentsPerEvent().keySet()
        return filter(events, eventsToFilter)
    }

    fun additionalEventsWithOffLabelExperimentalEvidence(evidence: AggregatedEvidence): Set<String?> {
        val eventsToFilter: MutableSet<String?> = Sets.newHashSet()
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence))
        eventsToFilter.addAll(actinInclusionEvents)
        eventsToFilter.addAll(additionalEventsWithOnLabelExperimentalEvidence(evidence))
        val events = evidence.offLabelExperimentalTreatmentsPerEvent().keySet()
        return filter(events, eventsToFilter)
    }

    companion object {
        fun fromEvaluatedCohorts(cohorts: List<EvaluatedCohort>): EvidenceInterpreter {
            val actinInclusionEvents: MutableSet<String?> = Sets.newHashSet()
            for (cohort in cohorts) {
                actinInclusionEvents.addAll(cohort.molecularEvents())
            }
            return EvidenceInterpreter(actinInclusionEvents)
        }

        private fun filter(events: Set<String>, eventsToFilter: Set<String?>): Set<String?> {
            val filtered: MutableSet<String?> = Sets.newHashSet()
            for (event in events) {
                if (!eventsToFilter.contains(event)) {
                    filtered.add(event)
                }
            }
            return filtered
        }
    }
}