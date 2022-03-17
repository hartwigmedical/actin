package com.hartwig.actin.algo.calendar;

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;

class CurrentDateProvider implements ReferenceDateProvider {

    @NotNull
    private final LocalDate currentDate = LocalDate.now();

    @NotNull
    @Override
    public LocalDate date() {
        return currentDate;
    }
}
