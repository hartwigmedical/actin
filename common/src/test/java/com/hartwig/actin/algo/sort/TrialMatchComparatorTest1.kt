package com.hartwig.actin.algo.sort

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class TrialMatchComparatorTest {
    @Test
    fun canSortTrialMatches() {
        val match1 = withId("Trial 1")
        val match2 = withId("Trial 2")
        val matches: List<TrialMatch> = Lists.newArrayList(match2, match1)
        matches.sort(TrialMatchComparator())
        Assert.assertEquals(match1, matches[0])
        Assert.assertEquals(match2, matches[1])
    }

    companion object {
        private fun withId(id: String): TrialMatch {
            return ImmutableTrialMatch.builder()
                .identification(
                    ImmutableTrialIdentification.builder()
                        .trialId(id)
                        .open(true)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build()
                )
                .isPotentiallyEligible(true)
                .build()
        }
    }
}