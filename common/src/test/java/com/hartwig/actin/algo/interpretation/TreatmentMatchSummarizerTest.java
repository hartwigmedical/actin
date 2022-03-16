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

        assertEquals(2, summary.trialCount());
        assertEquals(4, summary.cohortCount());

        assertEquals(1, summary.eligibleTrialMap().size());

        List<CohortMetadata> eligibleCohorts = summary.eligibleTrialMap().get(findByTrialId(summary, "Test Trial 1"));
        assertEquals(1, eligibleCohorts.size());
        assertNotNull(findByCohortId(eligibleCohorts, "B"));
    }

    @NotNull
    private static TrialIdentification findByTrialId(@NotNull TreatmentMatchSummary summary, @NotNull String trialIdToFind) {
        for (TrialIdentification identification : summary.eligibleTrialMap().keySet()) {
            if (identification.trialId().equals(trialIdToFind)) {
                return identification;
            }
        }

        throw new IllegalStateException("Could not find trial with id " + trialIdToFind);
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