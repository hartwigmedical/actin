package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.trial.datamodel.ImmutableTrialIdentification
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialMatchComparatorTest {
    @Test
    fun canSortTrialMatches() {
        val match1 = withId("Trial 1")
        val match2 = withId("Trial 2")
        val matches = listOf(match2, match1).sortedWith(TrialMatchComparator())
        assertThat(matches[0]).isEqualTo(match1)
        assertThat(matches[1]).isEqualTo(match2)
    }

    companion object {
        private fun withId(id: String): TrialMatch {
            return TrialMatch(
                identification = ImmutableTrialIdentification.builder()
                    .trialId(id)
                    .open(true)
                    .acronym(Strings.EMPTY)
                    .title(Strings.EMPTY)
                    .build(),
                isPotentiallyEligible = true,
                cohorts = emptyList(),
                evaluations = emptyMap()
            )
        }
    }
}