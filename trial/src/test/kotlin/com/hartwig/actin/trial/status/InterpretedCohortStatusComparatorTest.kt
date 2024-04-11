package com.hartwig.actin.trial.status

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InterpretedCohortStatusComparatorTest {

    @Test
    fun shouldRankCohortStatesCorrectly() {
        val openWithSlots = InterpretedCohortStatus(open = true, slotsAvailable = true)
        val openWithoutSlots = InterpretedCohortStatus(open = true, slotsAvailable = false)
        val closedWithoutSlots = InterpretedCohortStatus(open = false, slotsAvailable = false)

        val states = listOf(openWithoutSlots, openWithSlots, closedWithoutSlots).sortedWith(InterpretedCohortStatusComparator())
        assertThat(states).containsExactly(closedWithoutSlots, openWithoutSlots, openWithSlots)
    }
}