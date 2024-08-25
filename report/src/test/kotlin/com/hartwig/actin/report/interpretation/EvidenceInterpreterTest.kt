package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.approved
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.offLabelExperimental
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.onLabelExperimental
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.onLabelKnownResistant
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.onLabelPreclinical
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.onLabelSuspectResistant
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
            actionableTreatments = mapOf("approved" to setOf(approved()))
                    + mapOf(
                "on-label" to setOf(onLabelExperimental()),
                "approved" to setOf(approved())
            )
                    + mapOf(
                "off-label" to setOf(offLabelExperimental()),
                "on-label" to setOf(onLabelExperimental())
            )
                    + mapOf("pre-clinical" to setOf(onLabelPreclinical()))
                    + mapOf("known" to setOf(onLabelKnownResistant()))
                    + mapOf("suspect" to setOf(onLabelSuspectResistant())),
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
}