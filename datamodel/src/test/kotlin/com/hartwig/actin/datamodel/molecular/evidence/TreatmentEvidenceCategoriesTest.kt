package com.hartwig.actin.datamodel.molecular.evidence

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentEvidenceCategoriesTest {

    private val exhaustiveEvidence = TestClinicalEvidenceFactory.createExhaustive()

    @Test
    fun `Should filter approved evidence, on-label and A level evidence`() {
        val approved = TreatmentEvidenceCategories.approved(exhaustiveEvidence.treatmentEvidence)

        assertThat(approved).containsExactly(TestTreatmentEvidenceFactory.approved())
    }

    @Test
    fun `Should filter experimental for uncertain A level evidence and B level evidence`() {
        val experimental = TreatmentEvidenceCategories.experimental(exhaustiveEvidence.treatmentEvidence)

        assertThat(experimental).containsExactly(
            TestTreatmentEvidenceFactory.onLabelExperimental(), TestTreatmentEvidenceFactory.offLabelExperimental()
        )
    }

    @Test
    fun `Should filter preclinical for uncertain B level evidence and C and D level evidence`() {
        val preclinical = TreatmentEvidenceCategories.preclinical(exhaustiveEvidence.treatmentEvidence)

        assertThat(preclinical).containsExactly(
            TestTreatmentEvidenceFactory.onLabelPreclinical(), TestTreatmentEvidenceFactory.offLabelPreclinical()
        )
    }

    @Test
    fun `Should filter known resistant for certain A and B level evidence`() {
        val knownResistant = TreatmentEvidenceCategories.knownResistant(exhaustiveEvidence.treatmentEvidence)

        assertThat(knownResistant).containsExactly(
            TestTreatmentEvidenceFactory.onLabelKnownResistant(), TestTreatmentEvidenceFactory.offLabelKnownResistant()
        )
    }

    @Test
    fun `Should filter suspect resistant for uncertain A and B level evidence and C and D evidence`() {
        val suspectResistant = TreatmentEvidenceCategories.suspectResistant(exhaustiveEvidence.treatmentEvidence)

        assertThat(suspectResistant).containsExactly(
            TestTreatmentEvidenceFactory.onLabelSuspectResistant(), TestTreatmentEvidenceFactory.offLabelSuspectResistant()
        )
    }
}