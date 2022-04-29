package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class EvaluatedTrialComparatorTest {

    @Test
    public void canSortEvaluatedTrials() {
        EvaluatedTrial trial1 = create("trial 1", "cohort 1", false);
        EvaluatedTrial trial2 = create("trial 1", "cohort 2", false);
        EvaluatedTrial trial3 = create("trial 1", null, false);
        EvaluatedTrial trial4 = create("trial 2", "cohort 1", false);
        EvaluatedTrial trial5 = create("trial 5", "cohort 1", true);

        List<EvaluatedTrial> trials = Lists.newArrayList(trial1, trial2, trial3, trial4, trial5);
        trials.sort(new EvaluatedTrialComparator());

        assertEquals(trial5, trials.get(0));
        assertEquals(trial3, trials.get(1));
        assertEquals(trial1, trials.get(2));
        assertEquals(trial2, trials.get(3));
        assertEquals(trial4, trials.get(4));
    }

    @NotNull
    private static EvaluatedTrial create(@NotNull String trialId, @Nullable String cohort, boolean hasMolecularEvidence) {
        return ImmutableEvaluatedTrial.builder()
                .trialId(trialId)
                .acronym(Strings.EMPTY)
                .hasMolecularEvidence(hasMolecularEvidence)
                .cohort(cohort)
                .isPotentiallyEligible(false)
                .isOpen(false)
                .hasSlotsAvailable(false)
                .build();
    }
}