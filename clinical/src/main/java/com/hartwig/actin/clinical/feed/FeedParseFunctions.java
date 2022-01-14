package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Gender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FeedParseFunctions {

    private static final Set<DateTimeFormatter> DATE_FORMATS = Sets.newHashSet();

    private FeedParseFunctions() {
    }

    static {
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"));
    }

    @NotNull
    public static Gender parseGender(@NotNull String gender) {
        if (gender.equalsIgnoreCase("male")) {
            return Gender.MALE;
        } else if (gender.equalsIgnoreCase("female")) {
            return Gender.FEMALE;
        }

        throw new IllegalArgumentException("Could not resolve gender: " + gender);
    }

    @Nullable
    public static LocalDate parseOptionalDate(@NotNull String date) {
        return !date.isEmpty() ? parseDate(date) : null;
    }

    @NotNull
    public static LocalDate parseDate(@NotNull String date) {
        for (DateTimeFormatter format : DATE_FORMATS) {
            if (canBeInterpretedWithFormat(date, format)) {
                return LocalDate.parse(date, format);
            }
        }

        throw new IllegalArgumentException("Cannot transform string to date using any of the configured date formats: " + date);
    }

    private static boolean canBeInterpretedWithFormat(@NotNull String date, @NotNull DateTimeFormatter format) {
        try {
            LocalDate.parse(date, format);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    @Nullable
    public static Double parseOptionalDouble(@NotNull String number) {
        return !number.isEmpty() ? parseDouble(number) : null;
    }

    public static double parseDouble(@NotNull String number) {
        String formatted = number.replace(",", ".");
        if (formatted.indexOf(".") != formatted.lastIndexOf(".")) {
            throw new IllegalArgumentException("Invalid number: " + number);
        }
        return Double.parseDouble(formatted);
    }
}
