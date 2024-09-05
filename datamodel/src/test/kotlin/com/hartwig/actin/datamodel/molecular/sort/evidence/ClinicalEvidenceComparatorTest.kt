package com.hartwig.actin.datamodel.molecular.sort.evidence

import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withApprovedTreatment
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withExternalEligibleTrial
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withOffLabelExperimentalTreatment
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withOnLabelExperimentalTreatment
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withOnLabelKnownResistantTreatment
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withOnLabelPreClinicalTreatment
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.withSuspectResistantTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceComparatorTest {

    @Test
    fun `Should sort actionable evidences`() {
        val evidence1 = withApprovedTreatment("treatment")
        val evidence2 = withExternalEligibleTrial(TestClinicalEvidenceFactory.createTestExternalTrial())
        val evidence3 = withOnLabelExperimentalTreatment("treatment")
        val evidence4 = withOffLabelExperimentalTreatment("treatment")
        val evidence5 = withOnLabelPreClinicalTreatment("treatment")
        val evidence6 = withOnLabelKnownResistantTreatment("treatment")
        val evidence7 = withSuspectResistantTreatment("treatment")
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