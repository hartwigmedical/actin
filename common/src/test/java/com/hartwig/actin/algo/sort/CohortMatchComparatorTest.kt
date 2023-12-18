package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortMatchComparatorTest {
    @Test
    fun canSortCohortMatches() {
        val match1 = withId("A")
        val match2 = withId("B")
        val matches = listOf(match2, match1).sortedWith(CohortMatchComparator())
        assertThat(matches[0]).isEqualTo(match1)
        assertThat(matches[1]).isEqualTo(match2)
    }

    companion object {
        private fun withId(id: String): CohortMatch {
            return CohortMatch(
                metadata = ImmutableCohortMetadata.builder()
                    .cohortId(id)
                    .evaluable(true)
                    .open(true)
                    .slotsAvailable(true)
                    .blacklist(false)
                    .description(Strings.EMPTY)
                    .build(),
                isPotentiallyEligible = true,
                evaluations = emptyMap()
            )
        }
    }
}