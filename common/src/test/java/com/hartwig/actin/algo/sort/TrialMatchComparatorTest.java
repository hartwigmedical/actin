package com.hartwig.actin.algo.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialMatchComparatorTest {

    @Test
    public void canSortTrialEligibility() {
        TrialMatch eligibility1 = withId("Trial 1");
        TrialMatch eligibility2 = withId("Trial 2");

        List<TrialMatch> eligibilities = Lists.newArrayList(eligibility2, eligibility1);
        eligibilities.sort(new TrialMatchComparator());

        assertEquals(eligibility1, eligibilities.get(0));
        assertEquals(eligibility2, eligibilities.get(1));
    }

    @NotNull
    private static TrialMatch withId(@NotNull String id) {
        return ImmutableTrialMatch.builder()
                .identification(ImmutableTrialIdentification.builder().trialId(id).acronym(Strings.EMPTY).title(Strings.EMPTY).build())
                .isPotentiallyEligible(true)
                .build();
    }
}