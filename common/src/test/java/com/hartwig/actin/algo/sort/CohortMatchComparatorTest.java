package com.hartwig.actin.algo.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CohortMatchComparatorTest {

    @Test
    public void canSortCohortMatches() {
        CohortMatch match1 = withId("A");
        CohortMatch match2 = withId("B");

        List<CohortMatch> matches = Lists.newArrayList(match2, match1);
        matches.sort(new CohortMatchComparator());

        assertEquals(match1, matches.get(0));
        assertEquals(match2, matches.get(1));
    }

    @NotNull
    private static CohortMatch withId(@NotNull String id) {
        return ImmutableCohortMatch.builder()
                .metadata(ImmutableCohortMetadata.builder()
                        .cohortId(id)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .description(Strings.EMPTY)
                        .build())
                .isPotentiallyEligible(true)
                .build();
    }
}