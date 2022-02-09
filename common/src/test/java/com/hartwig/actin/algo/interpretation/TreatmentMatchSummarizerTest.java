package com.hartwig.actin.algo.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentMatchSummarizerTest {

    @Test
    public void canSummarizeTestData() {
        TreatmentMatchSummary summary = TreatmentMatchSummarizer.summarize(TestTreatmentMatchFactory.createProperTreatmentMatch());

        assertEquals(1, summary.trialCount());
        assertEquals(3, summary.cohortCount());

        assertEquals(1, summary.eligibleTrialMap().size());

        TrialIdentification firstTrial = summary.eligibleTrialMap().keySet().iterator().next();
        assertEquals("Test Trial", firstTrial.trialId());

        List<CohortMetadata> eligibleCohorts = summary.eligibleTrialMap().get(firstTrial);
        assertEquals(2, eligibleCohorts.size());
        assertNotNull(findByCohortId(eligibleCohorts, "B"));
        assertNotNull(findByCohortId(eligibleCohorts, "C"));
    }

    @NotNull
    private static CohortMetadata findByCohortId(@NotNull List<CohortMetadata> cohorts, @NotNull String cohortIdToFind) {
        for (CohortMetadata cohort : cohorts) {
            if (cohort.cohortId().equals(cohortIdToFind)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with id: " + cohortIdToFind);
    }

}