package com.hartwig.actin.molecular.datamodel.evidence

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceCategoriesTest {

    @Test
    fun `Should filter approved evidence, on-label and A level evidence`() {
        assertThat(ClinicalEvidenceCategories.approved(TestClinicalEvidenceFactory.createExhaustive().treatmentEvidence)).containsExactly(
            TestClinicalEvidenceFactory.approved()
        )
    }

    @Test
    fun `Should filter experimental for uncertain A level evidence and B level evidence`() {
        assertThat(ClinicalEvidenceCategories.experimental(TestClinicalEvidenceFactory.createExhaustive().treatmentEvidence)).containsExactly(
            TestClinicalEvidenceFactory.onLabelExperimental(), TestClinicalEvidenceFactory.offLabelExperimental()
        )
    }

    @Test
    fun `Should filter preclinical for uncertain B level evidence and C and D level evidence`() {
        assertThat(ClinicalEvidenceCategories.preclinical(TestClinicalEvidenceFactory.createExhaustive().treatmentEvidence)).containsExactly(
            TestClinicalEvidenceFactory.onLabelPreclinical(), TestClinicalEvidenceFactory.offLabelPreclinical()
        )
    }

    @Test
    fun `Should filter known resistant for certain A and B level evidence`() {
        assertThat(ClinicalEvidenceCategories.knownResistant(TestClinicalEvidenceFactory.createExhaustive().treatmentEvidence)).containsExactly(
            TestClinicalEvidenceFactory.onLabelKnownResistant(), TestClinicalEvidenceFactory.offLabelKnownResistant()
        )
    }

    @Test
    fun `Should filter known resistant for uncertain A and B level evidence and C and D evidence`() {
        assertThat(ClinicalEvidenceCategories.suspectResistant(TestClinicalEvidenceFactory.createExhaustive().treatmentEvidence)).containsExactly(
            TestClinicalEvidenceFactory.onLabelSuspectResistant(), TestClinicalEvidenceFactory.offLabelSuspectResistant()
        )
    }
}