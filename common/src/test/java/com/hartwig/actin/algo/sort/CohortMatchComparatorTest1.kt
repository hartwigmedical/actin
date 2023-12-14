package com.hartwig.actin.algo.sort

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class CohortMatchComparatorTest {
    @Test
    fun canSortCohortMatches() {
        val match1 = withId("A")
        val match2 = withId("B")
        val matches: List<CohortMatch> = Lists.newArrayList(match2, match1)
        matches.sort(CohortMatchComparator())
        Assert.assertEquals(match1, matches[0])
        Assert.assertEquals(match2, matches[1])
    }

    companion object {
        private fun withId(id: String): CohortMatch {
            return ImmutableCohortMatch.builder()
                .metadata(
                    ImmutableCohortMetadata.builder()
                        .cohortId(id)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .description(Strings.EMPTY)
                        .build()
                )
                .isPotentiallyEligible(true)
                .build()
        }
    }
}