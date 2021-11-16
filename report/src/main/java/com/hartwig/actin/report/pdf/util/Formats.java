package com.hartwig.actin.report.pdf.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringJoiner;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Style;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Formats {

    public static final String VALUE_UNKNOWN = "Unknown";
    public static final String DATE_UNKNOWN = "Date unknown";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private Formats() {
    }

    @NotNull
    public static String number(double number) {
        return NUMBER_FORMAT.format(number);
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

    @NotNull
    public static DeviceRgb fontColorForEvaluation(@NotNull Evaluation evaluation) {
        switch (evaluation) {
            case PASS:
            case PASS_BUT_WARN:
                return Styles.PALETTE_PASS;
            case FAIL:
                return Styles.PALETTE_FAIL;
            default:
                return Styles.PALETTE_UNCLEAR;
        }
    }
}
