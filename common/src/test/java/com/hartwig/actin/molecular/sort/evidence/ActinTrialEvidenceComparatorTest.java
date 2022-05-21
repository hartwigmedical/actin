package com.hartwig.actin.molecular.sort.evidence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidenceTestFactory;

import org.junit.Test;

public class ActinTrialEvidenceComparatorTest {

    @Test
    public void canSortActinTrialEvidences() {
        ActinTrialEvidence evidence1 = ActinTrialEvidenceTestFactory.builder().event("event A").trialAcronym("trial A").build();
        ActinTrialEvidence evidence2 = ActinTrialEvidenceTestFactory.builder().event("event A").trialAcronym("trial B").build();
        ActinTrialEvidence evidence3 =
                ActinTrialEvidenceTestFactory.builder().event("event A").trialAcronym("trial B").cohortId("A").build();
        ActinTrialEvidence evidence4 =
                ActinTrialEvidenceTestFactory.builder().event("event A").trialAcronym("trial A").isInclusionCriterion(false).build();
        ActinTrialEvidence evidence5 = ActinTrialEvidenceTestFactory.builder().event("event B").trialAcronym("trial A").build();

        List<ActinTrialEvidence> evidences = Lists.newArrayList(evidence1, evidence2, evidence3, evidence4, evidence5);
        evidences.sort(new ActinTrialEvidenceComparator());

        assertEquals(evidence1, evidences.get(0));
        assertEquals(evidence4, evidences.get(1));
        assertEquals(evidence3, evidences.get(2));
        assertEquals(evidence2, evidences.get(3));
        assertEquals(evidence5, evidences.get(4));
    }
}