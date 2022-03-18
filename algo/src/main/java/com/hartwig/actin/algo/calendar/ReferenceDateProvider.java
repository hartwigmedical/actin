package com.hartwig.actin.algo.calendar;

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;

public interface ReferenceDateProvider {

    @NotNull
    LocalDate date();

    boolean isLive();

    default int year() {
        return date().getYear();
    }

    default int month() {
        return date().getMonthValue();
    }
}
