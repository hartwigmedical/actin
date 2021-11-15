package com.hartwig.actin.algo.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;

import org.junit.Test;

public class TreatmentSummarizerTest {

    @Test
    public void canSummarizeTestData() {
        TreatmentSummary summary = TreatmentSummarizer.summarize(TestTreatmentMatchFactory.createProperTreatmentMatch());

        assertEquals(1, summary.trialCount());
        assertEquals(1, summary.eligibleTrialCount());
        assertEquals(3, summary.cohortCount());
        assertEquals(2, summary.eligibleCohortCount());
        assertEquals(2, summary.eligibleOpenCohortCount());
    }
}