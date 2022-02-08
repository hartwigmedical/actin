package com.hartwig.actin.algo.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;

import org.junit.Test;

public class TreatmentMatchSummarizerTest {

    @Test
    public void canSummarizeTestData() {
        TreatmentMatchSummary summary = TreatmentMatchSummarizer.summarize(TestTreatmentMatchFactory.createProperTreatmentMatch());

        assertEquals(1, summary.trialCount());
        assertEquals(1, summary.eligibleTrials().size());
        assertTrue(summary.eligibleTrials().contains("Test Trial (TEST-TRIAL)"));

        assertEquals(3, summary.cohortCount());
        assertEquals(2, summary.eligibleCohorts().size());
        assertTrue(summary.eligibleCohorts().contains("Test Trial - Cohort B"));
        assertTrue(summary.eligibleCohorts().contains("Test Trial - Cohort C"));

        assertEquals(2, summary.eligibleOpenCohorts().size());
        assertTrue(summary.eligibleOpenCohorts().contains("Test Trial - Cohort B"));
        assertTrue(summary.eligibleOpenCohorts().contains("Test Trial - Cohort C"));
    }
}