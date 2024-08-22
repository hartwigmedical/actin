package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ApplicableCancerType
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import com.hartwig.serve.datamodel.EvidenceLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceInterpreterTest {
    @Test
    fun shouldInterpretEvidence() {
        val cohortWithInclusion: EvaluatedCohort = evaluatedCohort(molecularEvents = setOf("inclusion"))
        val interpreter = EvidenceInterpreter.fromEvaluatedCohorts(listOf(cohortWithInclusion))
        val evidence = AggregatedEvidence(
            actionableTreatments = mapOf("approved" to setOf(treatment(ActinEvidenceCategory.APPROVED)))
                    + mapOf(
                "on-label" to setOf(treatment(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)),
                "approved" to setOf(treatment(ActinEvidenceCategory.APPROVED))
            )
                    + mapOf(
                "off-label" to setOf(treatment(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)),
                "on-label" to setOf(treatment(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL))
            )
                    + mapOf("pre-clinical" to setOf(treatment(ActinEvidenceCategory.PRE_CLINICAL)))
                    + mapOf("known" to setOf(treatment(ActinEvidenceCategory.KNOWN_RESISTANT)))
                    + mapOf("suspect" to setOf(treatment(ActinEvidenceCategory.SUSPECT_RESISTANT))),
            externalEligibleTrialsPerEvent = mapOf(
                "external" to setOf(TestExternalTrialFactory.createTestTrial()),
                "approved" to setOf(TestExternalTrialFactory.createTestTrial()),
                "inclusion" to setOf(TestExternalTrialFactory.createTestTrial())
            )
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

    private fun treatment(category: ActinEvidenceCategory) =
        TreatmentEvidence(
            treatment = "treatment", category = category, evidenceLevel = EvidenceLevel.A, sourceEvent = "",
            applicableCancerType = ApplicableCancerType("", emptySet())
        )
}