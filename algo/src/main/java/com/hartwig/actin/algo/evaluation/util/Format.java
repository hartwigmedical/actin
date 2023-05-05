package com.hartwig.actin.algo.evaluation.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

public final class Format {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private Format() {
    }

    @NotNull
    public static String concat(@NotNull Iterable<String> strings) {
        return StreamSupport.stream(strings.spliterator(), false).distinct().sorted().collect(Collectors.joining("; "));
    }

    @NotNull
    public static String date(@NotNull LocalDate date) {
        return DATE_FORMAT.format(date);
    }

    @NotNull
    public static String percentage(double fraction) {
        if (fraction < 0 || fraction > 1) {
            throw new IllegalArgumentException("Fraction provided that is not within 0 and 1: " + fraction);
        }
        return PERCENTAGE_FORMAT.format(fraction * 100);
    }
}
