package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class EvaluatedTrialComparatorTest {

    @Test
    public void canSortEvaluatedTrials() {
        EvaluatedTrial trial1 = create("trial 3", "cohort 2 + cohort 3", "Event C");
        EvaluatedTrial trial2 = create("trial 3", "cohort 1", "Event B");
        EvaluatedTrial trial3 = create("trial 5", "cohort 1", "Event D", "Event A");
        EvaluatedTrial trial4 = create("trial 5", "cohort 1", "Event C");
        EvaluatedTrial trial5 = create("trial 1", null);
        EvaluatedTrial trial6 = create("trial 1", "cohort 1");
        EvaluatedTrial trial7 = create("trial 1", "cohort 2");
        EvaluatedTrial trial8 = create("trial 2", "cohort 1");

        List<EvaluatedTrial> trials = Lists.newArrayList(trial7, trial4, trial2, trial8, trial1, trial6, trial3, trial5);
        trials.sort(new EvaluatedTrialComparator());

        assertEquals(trial1, trials.get(0));
        assertEquals(trial2, trials.get(1));
        assertEquals(trial3, trials.get(2));
        assertEquals(trial4, trials.get(3));
        assertEquals(trial5, trials.get(4));
        assertEquals(trial6, trials.get(5));
        assertEquals(trial7, trials.get(6));
        assertEquals(trial8, trials.get(7));
    }

    @NotNull
    private static EvaluatedTrial create(@NotNull String trialId, @Nullable String cohort, String... molecularEvents) {
        Set<String> molecularEventSet = Sets.newTreeSet(Ordering.natural());
        if (molecularEvents.length > 0) {
            molecularEventSet.addAll(Lists.newArrayList(molecularEvents));
        }

        return ImmutableEvaluatedTrial.builder()
                .trialId(trialId)
                .acronym(Strings.EMPTY)
                .molecularEvents(molecularEventSet)
                .cohort(cohort)
                .isPotentiallyEligible(false)
                .isOpen(false)
                .hasSlotsAvailable(false)
                .build();
    }
}