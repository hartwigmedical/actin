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
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DATE_FORMATS.add(DateTimeFormatter.ofPattern("d-M-yyyy HH:mm"));
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
        // One of the date formats ends with trailing milliseconds along with another 4 digits.
        String transformedDate = date.endsWith(".0000000") ? date.substring(0, date.length() - 8) : date;

        for (DateTimeFormatter format : DATE_FORMATS) {
            if (canBeInterpretedWithFormat(transformedDate, format)) {
                return LocalDate.parse(transformedDate, format);
            }
        }

        // If nothing works, just try with raw date on first format and probably trigger an exception.
        return LocalDate.parse(date, DATE_FORMATS.iterator().next());
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
        return Double.parseDouble(formatted);
    }
}
