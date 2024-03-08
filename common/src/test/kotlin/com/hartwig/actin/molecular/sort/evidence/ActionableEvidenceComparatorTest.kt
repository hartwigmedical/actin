package com.hartwig.actin.molecular.sort.evidence

import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withApprovedTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withExternalEligibleTrial
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withKnownResistantTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withOffLabelExperimentalTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withOnLabelExperimentalTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withPreClinicalTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withSuspectResistantTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionableEvidenceComparatorTest {

    @Test
    fun `Should sort actionable evidences`() {
        val evidence1 = withApprovedTreatment("treatment")
        val evidence2 = withExternalEligibleTrial(TestExternalTrialFactory.createTestTrial())
        val evidence3 = withOnLabelExperimentalTreatment("treatment")
        val evidence4 = withOffLabelExperimentalTreatment("treatment")
        val evidence5 = withPreClinicalTreatment("treatment")
        val evidence6 = withKnownResistantTreatment("treatment")
        val evidence7 = withSuspectResistantTreatment("treatment")
        val evidences =
            listOf(evidence5, evidence4, evidence1, evidence2, evidence6, evidence7, evidence3).sortedWith(ActionableEvidenceComparator())

        assertThat(evidences[0]).isEqualTo(evidence1)
        assertThat(evidences[1]).isEqualTo(evidence2)
        assertThat(evidences[2]).isEqualTo(evidence3)
        assertThat(evidences[3]).isEqualTo(evidence4)
        assertThat(evidences[4]).isEqualTo(evidence5)
        assertThat(evidences[5]).isEqualTo(evidence6)
        assertThat(evidences[6]).isEqualTo(evidence7)
    }
}