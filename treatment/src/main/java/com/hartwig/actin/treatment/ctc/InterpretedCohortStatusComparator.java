package com.hartwig.actin.treatment.ctc;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

class InterpretedCohortStatusComparator implements Comparator<InterpretedCohortStatus> {

    @Override
    public int compare(@NotNull InterpretedCohortStatus status1, @NotNull InterpretedCohortStatus status2) {
        if (status1.open() == status2.open()) {
            return Boolean.compare(status1.slotsAvailable(), status2.slotsAvailable());
        }

        return Boolean.compare(status1.open(), status2.open());
    }
}
