package com.hartwig.actin.report.pdf.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Formats {

    public static final String UNKNOWN = "Unknown";

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
            return UNKNOWN;
        }

        return bool ? "Yes" : "No";
    }

    @NotNull
    public static StringJoiner commaJoiner() {
        return new StringJoiner(", ");
    }

    @NotNull
    public static String valueOrDefault(@NotNull String value, @NotNull String defaultValue) {
        return !value.isEmpty() ? value : defaultValue;
    }
}
