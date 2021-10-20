package com.hartwig.actin.report.pdf.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

import com.itextpdf.layout.Style;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Formats {

    public static final String VALUE_UNKNOWN = "Unknown";
    public static final String DATE_UNKNOWN = "Date unknown";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private Formats() {
    }

    @NotNull
    public static String date(@Nullable LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : DATE_UNKNOWN;
    }

    @NotNull
    public static String yesNoUnknown(@Nullable Boolean bool) {
        if (bool == null) {
            return VALUE_UNKNOWN;
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

    @NotNull
    public static Style styleForTableValue(@NotNull String value) {
        return !value.equals(Formats.VALUE_UNKNOWN) ? Styles.tableValueHighlightStyle() : Styles.tableValueUnknownStyle();
    }
}
