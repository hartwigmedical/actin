package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.interpretation.AggregatedEvidence
import com.hartwig.actin.molecular.interpretation.ImmutableAggregatedEvidence
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceInterpreterTest {
    @Test
    fun shouldInterpretEvidence() {
        val cohortWithInclusion: EvaluatedCohort = evaluatedCohort(molecularEvents = setOf("inclusion"))
        val interpreter = EvidenceInterpreter.fromEvaluatedCohorts(listOf(cohortWithInclusion))
        val evidence: AggregatedEvidence = ImmutableAggregatedEvidence.builder()
            .putApprovedTreatmentsPerEvent("approved", "treatment")
            .putExternalEligibleTrialsPerEvent("external", "treatment")
            .putExternalEligibleTrialsPerEvent("approved", "treatment")
            .putExternalEligibleTrialsPerEvent("inclusion", "treatment")
            .putOnLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
            .putOnLabelExperimentalTreatmentsPerEvent("approved", "treatment")
            .putOffLabelExperimentalTreatmentsPerEvent("off-label", "treatment")
            .putOffLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
            .putPreClinicalTreatmentsPerEvent("pre-clinical", "treatment")
            .putKnownResistantTreatmentsPerEvent("known", "treatment")
            .putSuspectResistanceTreatmentsPerEvent("suspect", "treatment")
            .build()

        val approved = interpreter.eventsWithApprovedEvidence(evidence)
        assertThat(approved).hasSize(1)
        assertThat(approved.contains("approved")).isTrue

        val external = interpreter.additionalEventsWithExternalTrialEvidence(evidence)
        assertThat(external).hasSize(1)
        assertThat(external.contains("external")).isTrue

        val onLabel = interpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence)
        assertThat(onLabel).hasSize(1)
        assertThat(onLabel.contains("on-label")).isTrue

        val offLabel = interpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence)
        assertThat(offLabel).hasSize(1)
        assertThat(offLabel.contains("off-label")).isTrue
    }
}