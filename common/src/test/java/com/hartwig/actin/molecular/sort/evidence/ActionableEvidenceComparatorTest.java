package com.hartwig.actin.molecular.sort.evidence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.junit.Test;

public class ActionableEvidenceComparatorTest {

    @Test
    public void canSortActionableEvidences() {
        ActionableEvidence evidence1 = TestActionableEvidenceFactory.withApprovedTreatment("treatment");
        ActionableEvidence evidence2 = TestActionableEvidenceFactory.withExternalEligibleTrial("treatment");
        ActionableEvidence evidence3 = TestActionableEvidenceFactory.withOnLabelExperimentalTreatment("treatment");
        ActionableEvidence evidence4 = TestActionableEvidenceFactory.withOffLabelExperimentalTreatment("treatment");
        ActionableEvidence evidence5 = TestActionableEvidenceFactory.withPreClinicalTreatment("treatment");
        ActionableEvidence evidence6 = TestActionableEvidenceFactory.withKnownResistantTreatment("treatment");
        ActionableEvidence evidence7 = TestActionableEvidenceFactory.withSuspectResistantTreatment("treatment");

        List<ActionableEvidence> evidences =
                Lists.newArrayList(evidence5, evidence4, evidence1, evidence2, evidence6, evidence7, evidence3);
        evidences.sort(new ActionableEvidenceComparator());

        assertEquals(evidence1, evidences.get(0));
        assertEquals(evidence2, evidences.get(1));
        assertEquals(evidence3, evidences.get(2));
        assertEquals(evidence4, evidences.get(3));
        assertEquals(evidence5, evidences.get(4));
        assertEquals(evidence6, evidences.get(5));
        assertEquals(evidence7, evidences.get(6));
    }
}