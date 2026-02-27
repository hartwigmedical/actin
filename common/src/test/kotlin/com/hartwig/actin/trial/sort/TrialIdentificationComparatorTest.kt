package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.TrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialIdentificationComparatorTest {

    @Test
    fun `Should sort trial identifications`() {
        val identification1 = identification("1", "First", "Real First")
        val identification2 = identification("1", "First", "Wants to be first")
        val identification3 = identification("1", "Second", "Second")
        val identification4 = identification("2", "Third", "Third")
        val identifications = listOf(identification1, identification2, identification3, identification4)
            .sortedWith(TrialIdentificationComparator())

        assertThat(identifications[0]).isEqualTo(identification1)
        assertThat(identifications[1]).isEqualTo(identification2)
        assertThat(identifications[2]).isEqualTo(identification3)
        assertThat(identifications[3]).isEqualTo(identification4)
    }

    private fun identification(trialId: String, acronym: String, title: String): TrialIdentification {
        return TrialIdentification(
            trialId = trialId,
            open = true,
            acronym = acronym,
            title = title,
            nctId = null,
            phase = null,
            source = null,
            sourceId = null,
            locations = emptySet(),
            url = null
        )
    }
}