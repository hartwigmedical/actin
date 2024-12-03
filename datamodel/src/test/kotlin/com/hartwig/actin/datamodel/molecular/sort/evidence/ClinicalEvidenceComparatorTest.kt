package com.hartwig.actin.datamodel.molecular.sort.evidence

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceComparatorTest {

    @Test
    fun `Should sort clinical evidence`() {
        val evidence1 = TestClinicalEvidenceFactory.withApprovedTreatment("treatment")
        val evidence2 = TestClinicalEvidenceFactory.withEligibleTrial(TestExternalTrialFactory.createTestTrial())
        val evidence3 = TestClinicalEvidenceFactory.withOnLabelExperimentalTreatment("treatment")
        val evidence4 = TestClinicalEvidenceFactory.withOffLabelExperimentalTreatment("treatment")
        val evidence5 = TestClinicalEvidenceFactory.withOnLabelPreClinicalTreatment("treatment")
        val evidence6 = TestClinicalEvidenceFactory.withOnLabelKnownResistantTreatment("treatment")
        val evidence7 = TestClinicalEvidenceFactory.withOnLabelSuspectResistantTreatment("treatment")

        val evidences =
            listOf(evidence5, evidence4, evidence1, evidence2, evidence6, evidence7, evidence3).sortedWith(ClinicalEvidenceComparator())

        assertThat(evidences[0]).isEqualTo(evidence1)
        assertThat(evidences[1]).isEqualTo(evidence2)
        assertThat(evidences[2]).isEqualTo(evidence3)
        assertThat(evidences[3]).isEqualTo(evidence4)
        assertThat(evidences[4]).isEqualTo(evidence5)
        assertThat(evidences[5]).isEqualTo(evidence6)
        assertThat(evidences[6]).isEqualTo(evidence7)
    }
}