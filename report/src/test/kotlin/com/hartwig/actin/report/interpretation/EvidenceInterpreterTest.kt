package com.hartwig.actin.report.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEligibleTrial
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
            .putExternalEligibleTrialsPerEvent("external", ImmutableEligibleTrial.builder()
                .title("treatment")
                .countries(Sets.newHashSet("Netherlands", "Germany"))
                .website("https://clinicaltrials.gov/study/NCT04898634")
                .build())
            .putExternalEligibleTrialsPerEvent("approved", ImmutableEligibleTrial.builder()
                .title("treatment")
                .countries(Sets.newHashSet("Netherlands", "Germany"))
                .website("https://clinicaltrials.gov/study/NCT04898634")
                .build())
            .putExternalEligibleTrialsPerEvent("inclusion", ImmutableEligibleTrial.builder()
                .title("treatment")
                .countries(Sets.newHashSet("Netherlands", "Germany"))
                .website("https://clinicaltrials.gov/study/NCT04898634")
                .build())
            .putOnLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
            .putOnLabelExperimentalTreatmentsPerEvent("approved", "treatment")
            .putOffLabelExperimentalTreatmentsPerEvent("off-label", "treatment")
            .putOffLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
            .putPreClinicalTreatmentsPerEvent("pre-clinical", "treatment")
            .putKnownResistantTreatmentsPerEvent("known", "treatment")
            .putSuspectResistanceTreatmentsPerEvent("suspect", "treatment")
            .build()

        val approved = interpreter.eventsWithApprovedEvidence(evidence)
        assertThat(approved).containsExactly("approved")

        val external = interpreter.additionalEventsWithExternalTrialEvidence(evidence)
        assertThat(external).containsExactly("external")

        val onLabel = interpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence)
        assertThat(onLabel).containsExactly("on-label")

        val offLabel = interpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence)
        assertThat(offLabel).containsExactly("off-label")
    }
}