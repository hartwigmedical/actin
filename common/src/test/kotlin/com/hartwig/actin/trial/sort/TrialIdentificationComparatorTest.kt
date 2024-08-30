package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.TrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialIdentificationComparatorTest {

    @Test
    fun `Should sort trial identifications`() {
        val identification1 = TrialIdentification(trialId = "1", open = true, acronym = "First", title = "Real First", nctId = null)
        val identification2 = TrialIdentification(trialId = "1", open = true, acronym = "First", title = "Wants to be first", nctId = null)
        val identification3 = TrialIdentification(trialId = "1", open = true, acronym = "Second", title = "Second", nctId = null)
        val identification4 = TrialIdentification(trialId = "2", open = true, acronym = "Third", title = "Third", nctId = null)
        val identifications = listOf(identification1, identification2, identification3, identification4)
            .sortedWith(TrialIdentificationComparator())

        assertThat(identifications[0]).isEqualTo(identification1)
        assertThat(identifications[1]).isEqualTo(identification2)
        assertThat(identifications[2]).isEqualTo(identification3)
        assertThat(identifications[3]).isEqualTo(identification4)
    }
}