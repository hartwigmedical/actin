package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class EvaluatedCohortComparatorTest {

    @NotNull
    private static EvaluatedCohort create(@NotNull String trialId, @Nullable String cohort, boolean hasSlotsAvailable,
            String... molecularEvents) {
        Set<String> molecularEventSet = Sets.newTreeSet(Ordering.natural());
        if (molecularEvents.length > 0) {
            molecularEventSet.addAll(Lists.newArrayList(molecularEvents));
        }

        return ImmutableEvaluatedCohort.builder()
                .trialId(trialId)
                .acronym(Strings.EMPTY)
                .molecularEvents(molecularEventSet)
                .cohort(cohort)
                .isPotentiallyEligible(false)
                .isOpen(false)
                .hasSlotsAvailable(hasSlotsAvailable)
                .build();
    }

    @Test
    public void canSortEvaluatedTrials() {
        List<EvaluatedCohort> trials = Arrays.asList(create("trial 7", "cohort 1", true),
                create("trial 3", "cohort 2 + cohort 3", false, "Event C"),
                create("trial 3", "cohort 1", false, "Event B"),
                create("trial 5", "cohort 1", false, "Event D", "Event A"),
                create("trial 5", "cohort 1", false, "Event C"),
                create("trial 1", null, false),
                create("trial 1", "cohort 1", false),
                create("trial 1", "cohort 2", false),
                create("trial 2", "cohort 1", false));

        List<EvaluatedCohort> trialList = Lists.newArrayList(trials.get(7),
                trials.get(4),
                trials.get(2),
                trials.get(8),
                trials.get(1),
                trials.get(6),
                trials.get(0),
                trials.get(3),
                trials.get(5));
        trialList.sort(new EvaluatedCohortComparator());
        Iterator<EvaluatedCohort> trialIterator = trialList.iterator();

        trials.forEach(trial -> assertEquals(trial, trialIterator.next()));
    }
}