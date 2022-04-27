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
    public void canSortTrialMatches() {
        TrialMatch match1 = withId("Trial 1");
        TrialMatch match2 = withId("Trial 2");

        List<TrialMatch> matches = Lists.newArrayList(match2, match1);
        matches.sort(new TrialMatchComparator());

        assertEquals(match1, matches.get(0));
        assertEquals(match2, matches.get(1));
    }

    @NotNull
    private static TrialMatch withId(@NotNull String id) {
        return ImmutableTrialMatch.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId(id)
                        .open(true)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build())
                .isPotentiallyEligible(true)
                .build();
    }
}