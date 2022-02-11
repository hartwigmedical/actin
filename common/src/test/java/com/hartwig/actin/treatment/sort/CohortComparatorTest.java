package com.hartwig.actin.treatment.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CohortComparatorTest {

    @Test
    public void canSortCohorts() {
        Cohort cohort1 = withId("A");
        Cohort cohort2 = withId("B");

        List<Cohort> cohorts = Lists.newArrayList(cohort2, cohort1);
        cohorts.sort(new CohortComparator());

        assertEquals(cohort1, cohorts.get(0));
        assertEquals(cohort2, cohorts.get(1));
    }

    @NotNull
    private static Cohort withId(@NotNull String id) {
        return ImmutableCohort.builder()
                .metadata(ImmutableCohortMetadata.builder().cohortId(id).open(true).blacklist(false).description(Strings.EMPTY).build())
                .build();
    }
}