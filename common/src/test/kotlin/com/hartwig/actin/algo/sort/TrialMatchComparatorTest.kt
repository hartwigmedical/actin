package com.hartwig.actin.algo.sort

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.TrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialMatchComparatorTest {

    @Test
    fun `Should sort trial matches properly`() {
        val match1 = withId("Trial 1")
        val match2 = withId("Trial 2")
        val matches = listOf(match2, match1).sortedWith(TrialMatchComparator())
        assertThat(matches[0]).isEqualTo(match1)
        assertThat(matches[1]).isEqualTo(match2)
    }

    private fun withId(id: String): TrialMatch {
        return TrialMatch(
            identification = TrialIdentification(
                nctId = id,
                open = true,
                acronym = "",
                title = "",
            ),
            isPotentiallyEligible = true,
            cohorts = emptyList(),
            nonEvaluableCohorts = emptyList(),
            evaluations = emptyMap()
        )
    }
}