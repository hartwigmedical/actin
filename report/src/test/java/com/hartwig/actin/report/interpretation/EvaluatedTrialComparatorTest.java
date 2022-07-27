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
        EvaluatedTrial trial1 = create("trial 1", "cohort 1");
        EvaluatedTrial trial2 = create("trial 1", "cohort 2");
        EvaluatedTrial trial3 = create("trial 1", null);
        EvaluatedTrial trial4 = create("trial 2", "cohort 1");
        EvaluatedTrial trial5 = create("trial 3", "cohort 1", "Event B");
        EvaluatedTrial trial6 = create("trial 3", "cohort 2 + cohort 3", "Event C");
        EvaluatedTrial trial7 = create("trial 5", "cohort 1", "Event C");
        EvaluatedTrial trial8 = create("trial 5", "cohort 1", "Event D", "Event A");

        List<EvaluatedTrial> trials = Lists.newArrayList(trial1, trial2, trial3, trial4, trial5, trial6, trial7, trial8);
        trials.sort(new EvaluatedTrialComparator());

        assertEquals(trial6, trials.get(0));
        assertEquals(trial5, trials.get(1));
        assertEquals(trial8, trials.get(2));
        assertEquals(trial7, trials.get(3));
        assertEquals(trial3, trials.get(4));
        assertEquals(trial1, trials.get(5));
        assertEquals(trial2, trials.get(6));
        assertEquals(trial4, trials.get(7));
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