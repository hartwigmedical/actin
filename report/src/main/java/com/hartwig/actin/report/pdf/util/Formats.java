package com.hartwig.actin.report.pdf.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Style;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Formats {

    public static final String VALUE_UNKNOWN = "Unknown";
    public static final String VALUE_COMING_SOON = "Coming soon";

    private static final Set<String> NON_HIGHLIGHT_VALUES = Sets.newHashSet(VALUE_UNKNOWN, VALUE_COMING_SOON);

    public static final String DATE_UNKNOWN = "Date unknown";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DecimalFormat TWO_DIGIT_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final DecimalFormat SINGLE_DIGIT_FORMAT = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final DecimalFormat NO_DIGIT_FORMAT = new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private Formats() {
    }

    @NotNull
    public static String twoDigitNumber(double number) {
        return TWO_DIGIT_FORMAT.format(number);
    }

    @NotNull
    public static String singleDigitNumber(double number) {
        return SINGLE_DIGIT_FORMAT.format(number);
    }

    @NotNull
    public static String noDigitNumber(double number) {
        return NO_DIGIT_FORMAT.format(number);
    }

    @NotNull
    public static String percentage(double number) {
        return PERCENTAGE_FORMAT.format(number * 100);
    }

    @NotNull
    public static String date(@Nullable LocalDate date) {
        return date(date, DATE_UNKNOWN);
    }

    @NotNull
    public static String date(@Nullable LocalDate date, @NotNull String fallback) {
        return date != null ? DATE_FORMAT.format(date) : fallback;
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
    public static StringJoiner semicolonJoiner() {
        return new StringJoiner("; ");
    }

    @NotNull
    public static String valueOrDefault(@NotNull String value, @NotNull String defaultValue) {
        return !value.isEmpty() ? value : defaultValue;
    }

    @NotNull
    public static Style styleForTableValue(@NotNull String value) {
        return !NON_HIGHLIGHT_VALUES.contains(value) ? Styles.tableHighlightStyle() : Styles.tableUnknownStyle();
    }

    @NotNull
    public static DeviceRgb fontColorForEvaluation(@NotNull EvaluationResult evaluation) {
        switch (evaluation) {
            case PASS:
                return Styles.PALETTE_EVALUATION_PASS;
            case WARN:
                return Styles.PALETTE_EVALUATION_WARN;
            case FAIL:
                return Styles.PALETTE_EVALUATION_FAILED;
            default:
                return Styles.PALETTE_EVALUATION_UNCLEAR;
        }
    }

    @NotNull
    public static DeviceRgb fontColorForYesNo(@NotNull String yesNo) {
        switch (yesNo) {
            case "Yes":
                return Styles.PALETTE_YES_OR_NO_YES;
            case "No":
                return Styles.PALETTE_YES_OR_NO_NO;
            default:
                return Styles.PALETTE_YES_OR_NO_UNCLEAR;
        }
    }
}
