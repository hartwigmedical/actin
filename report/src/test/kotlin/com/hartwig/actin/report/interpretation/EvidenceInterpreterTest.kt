package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.treatment
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
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
            actionableTreatments = mapOf("approved" to setOf(createTreatment(ActinEvidenceCategory.APPROVED)))
                    + mapOf(
                "on-label" to setOf(createTreatment(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)),
                "approved" to setOf(createTreatment(ActinEvidenceCategory.APPROVED))
            )
                    + mapOf(
                "off-label" to setOf(createTreatment(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)),
                "on-label" to setOf(createTreatment(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL))
            )
                    + mapOf("pre-clinical" to setOf(createTreatment(ActinEvidenceCategory.PRE_CLINICAL)))
                    + mapOf("known" to setOf(createTreatment(ActinEvidenceCategory.KNOWN_RESISTANT)))
                    + mapOf("suspect" to setOf(createTreatment(ActinEvidenceCategory.SUSPECT_RESISTANT))),
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

    private fun createTreatment(category: ActinEvidenceCategory) =
        treatment(treatment = "treatment", category = category, evidenceLevel = EvidenceLevel.A)
}