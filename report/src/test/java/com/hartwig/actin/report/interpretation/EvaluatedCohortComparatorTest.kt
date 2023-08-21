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

    @Test
    public void canSortEvaluatedCohorts() {
        List<EvaluatedCohort> cohorts = Arrays.asList(create("trial 7", "cohort 1", true),
                create("trial 3", "cohort 2 + cohort 3", false, "Event C"),
                create("trial 3", "cohort 1", false, "Event B"),
                create("trial 5", "cohort 1", false, "Event D", "Event A"),
                create("trial 5", "cohort 1", false, "Event C"),
                create("trial 1", null, false),
                create("trial 1", "cohort 1", false),
                create("trial 1", "cohort 2", false),
                create("trial 2", "cohort 1", false));

        List<EvaluatedCohort> cohortList = Lists.newArrayList(cohorts.get(7),
                cohorts.get(4),
                cohorts.get(2),
                cohorts.get(8),
                cohorts.get(1),
                cohorts.get(6),
                cohorts.get(0),
                cohorts.get(3),
                cohorts.get(5));
        cohortList.sort(new EvaluatedCohortComparator());
        Iterator<EvaluatedCohort> cohortIterator = cohortList.iterator();

        cohorts.forEach(cohort -> assertEquals(cohort, cohortIterator.next()));
    }

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
}