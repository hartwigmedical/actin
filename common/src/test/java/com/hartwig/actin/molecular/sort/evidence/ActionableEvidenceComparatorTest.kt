package com.hartwig.actin.molecular.sort.evidence

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withApprovedTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withExternalEligibleTrial
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withKnownResistantTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withOffLabelExperimentalTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withOnLabelExperimentalTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withPreClinicalTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withSuspectResistantTreatment
import org.junit.Assert
import org.junit.Test

class ActionableEvidenceComparatorTest {
    @Test
    fun canSortActionableEvidences() {
        val evidence1 = withApprovedTreatment("treatment")
        val evidence2 = withExternalEligibleTrial("treatment")
        val evidence3 = withOnLabelExperimentalTreatment("treatment")
        val evidence4 = withOffLabelExperimentalTreatment("treatment")
        val evidence5 = withPreClinicalTreatment("treatment")
        val evidence6 = withKnownResistantTreatment("treatment")
        val evidence7 = withSuspectResistantTreatment("treatment")
        val evidences: List<ActionableEvidence> =
            Lists.newArrayList(evidence5, evidence4, evidence1, evidence2, evidence6, evidence7, evidence3)
        evidences.sort(ActionableEvidenceComparator())
        Assert.assertEquals(evidence1, evidences[0])
        Assert.assertEquals(evidence2, evidences[1])
        Assert.assertEquals(evidence3, evidences[2])
        Assert.assertEquals(evidence4, evidences[3])
        Assert.assertEquals(evidence5, evidences[4])
        Assert.assertEquals(evidence6, evidences[5])
        Assert.assertEquals(evidence7, evidences[6])
    }
}