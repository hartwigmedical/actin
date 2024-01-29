package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceInterpreterTest {
    @Test
    fun shouldInterpretEvidence() {
        val cohortWithInclusion: EvaluatedCohort = evaluatedCohort(molecularEvents = setOf("inclusion"))
        val interpreter = EvidenceInterpreter.fromEvaluatedCohorts(listOf(cohortWithInclusion))
        val evidence = AggregatedEvidence(
            approvedTreatmentsPerEvent = mapOf("approved" to listOf("treatment")),
            externalEligibleTrialsPerEvent = mapOf(
                "external" to listOf(TestExternalTrialFactory.createTestTrial()),
                "approved" to listOf(TestExternalTrialFactory.createTestTrial()),
                "inclusion" to listOf(TestExternalTrialFactory.createTestTrial())
            ),
            onLabelExperimentalTreatmentsPerEvent = mapOf("on-label" to listOf("treatment"), "approved" to listOf("treatment")),
            offLabelExperimentalTreatmentsPerEvent = mapOf("off-label" to listOf("treatment"), "on-label" to listOf("treatment")),
            preClinicalTreatmentsPerEvent = mapOf("pre-clinical" to listOf("treatment")),
            knownResistantTreatmentsPerEvent = mapOf("known" to listOf("treatment")),
            suspectResistantTreatmentsPerEvent = mapOf("suspect" to listOf("treatment"))
        )

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