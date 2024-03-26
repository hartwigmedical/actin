package com.hartwig.actin.trial.status

internal class InterpretedCohortStatusComparator : Comparator<InterpretedCohortStatus> {

    override fun compare(status1: InterpretedCohortStatus, status2: InterpretedCohortStatus): Int {
        return if (status1.open == status2.open) {
            compareValues(status1.slotsAvailable, status2.slotsAvailable)
        } else {
            compareValues(status1.open, status2.open)
        }
    }
}