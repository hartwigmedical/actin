package com.hartwig.actin.algo.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.ImmutableCohortEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CohortEligibilityComparatorTest {

    @Test
    public void canSortCohortEligibility() {
        CohortEligibility eligibility1 = withId("A");
        CohortEligibility eligibility2 = withId("B");

        List<CohortEligibility> eligibilities = Lists.newArrayList(eligibility2, eligibility1);
        eligibilities.sort(new CohortEligibilityComparator());

        assertEquals(eligibility1, eligibilities.get(0));
        assertEquals(eligibility2, eligibilities.get(1));
    }

    @NotNull
    private static CohortEligibility withId(@NotNull String id) {
        return ImmutableCohortEligibility.builder()
                .metadata(ImmutableCohortMetadata.builder().cohortId(id).open(true).description(Strings.EMPTY).build())
                .overallEvaluation(Evaluation.PASS)
                .build();
    }

}