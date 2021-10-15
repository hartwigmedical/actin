package com.hartwig.actin.report.pdf.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;

public final class Formats {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private Formats() {
    }

    @NotNull
    public static String date(@NotNull LocalDate date) {
        return DATE_FORMAT.format(date);
    }
}
