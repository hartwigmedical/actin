package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.trial.datamodel.CohortMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortMatchComparatorTest {

    @Test
    fun `Should sort cohort matches properly`() {
        val match1 = withId("A")
        val match2 = withId("B")
        val matches = listOf(match2, match1).sortedWith(CohortMatchComparator())
        assertThat(matches[0]).isEqualTo(match1)
        assertThat(matches[1]).isEqualTo(match2)
    }

    private fun withId(id: String): CohortMatch {
        return CohortMatch(
            metadata = CohortMetadata(
                cohortId = id,
                evaluable = true,
                open = true,
                slotsAvailable = true,
                blacklist = false,
                description = "",
            ),
            isPotentiallyEligible = true,
            evaluations = emptyMap()
        )
    }
}