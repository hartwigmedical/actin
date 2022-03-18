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
    public void canSortCohortEligibility() {
        CohortMatch eligibility1 = withId("A");
        CohortMatch eligibility2 = withId("B");

        List<CohortMatch> eligibilities = Lists.newArrayList(eligibility2, eligibility1);
        eligibilities.sort(new CohortMatchComparator());

        assertEquals(eligibility1, eligibilities.get(0));
        assertEquals(eligibility2, eligibilities.get(1));
    }

    @NotNull
    private static CohortMatch withId(@NotNull String id) {
        return ImmutableCohortMatch.builder()
                .metadata(ImmutableCohortMetadata.builder().cohortId(id).open(true).blacklist(false).description(Strings.EMPTY).build())
                .isPotentiallyEligible(true)
                .build();
    }
}