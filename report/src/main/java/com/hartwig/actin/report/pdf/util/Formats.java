package com.hartwig.actin.report.pdf.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Formats {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private Formats() {
    }

    @NotNull
    public static String date(@NotNull LocalDate date) {
        return DATE_FORMAT.format(date);
    }

    @NotNull
    public static String yesNoUnknown(@Nullable Boolean bool) {
        if (bool == null) {
            return "Unknown";
        }

        return bool ? "Yes" : "No";
    }

    @NotNull
    public static StringJoiner stringJoiner() {
        return new StringJoiner(", ");
    }
}
