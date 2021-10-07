package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hartwig.actin.clinical.datamodel.Sex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FeedUtil {

    private FeedUtil() {
    }

    @NotNull
    public static Sex parseSex(@NotNull String sex) {
        if (sex.equalsIgnoreCase("male")) {
            return Sex.MALE;
        } else if (sex.equalsIgnoreCase("female")) {
            return Sex.FEMALE;
        }

        throw new IllegalArgumentException("Could not resolve sex: " + sex);
    }

    @Nullable
    public static LocalDate parseOptionalDate(@NotNull String date, @NotNull DateTimeFormatter format) {
        return !date.isEmpty() ? parseDate(date, format) : null;
    }

    @NotNull
    public static LocalDate parseDate(@NotNull String date, @NotNull DateTimeFormatter format) {
        return LocalDate.parse(date, format);
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
