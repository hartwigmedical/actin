package com.hartwig.actin.clinical.curation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CurationUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String UNKNOWN = "unknown";
    private static final String IGNORE = "<ignore>";
    private static final String DOID_SEPARATOR = ";";

    private CurationUtil() {
    }

    public static boolean ignore(@NotNull String input) {
        return input.equals(IGNORE);
    }

    @Nullable
    public static String capitalizeFirstLetterOnly(@Nullable String string) {
        if (string == null) {
            return null;
        }

        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    @Nullable
    public static String optionalString(@NotNull String string) {
        return hasValue(string) ? string : null;
    }

    @NotNull
    public static Set<String> parseDOID(@NotNull String doidString) {
        if (hasValue(doidString)) {
            return Sets.newHashSet(doidString.split(DOID_SEPARATOR));
        } else {
            return Sets.newHashSet();
        }
    }

    @Nullable
    public static Boolean parseOptionalBoolean(@NotNull String bool) {
        return hasValue(bool) ? parseBoolean(bool) : null;
    }

    public static boolean parseBoolean(@NotNull String bool) {
        if (bool.equals("1")) {
            return true;
        } else if (bool.equals("0")) {
            return false;
        } else {
            throw new IllegalArgumentException("Cannot convert curation value to boolean: '" + bool + "'");
        }
    }

    @Nullable
    public static LocalDate parseOptionalDate(@NotNull String date) {
        return hasValue(date) ? parseDate(date) : null;
    }

    @NotNull
    public static LocalDate parseDate(@NotNull String date) {
        return LocalDate.parse(date, DATE_FORMAT);
    }

    @Nullable
    public static Integer parseOptionalInteger(@NotNull String integer) {
        return hasValue(integer) ? parseInteger(integer) : null;
    }

    public static int parseInteger(@NotNull String integer) {
        return Integer.parseInt(integer);
    }

    @Nullable
    public static Double parseOptionalDouble(@NotNull String doubleString) {
        return hasValue(doubleString) ? parseDouble(doubleString) : null;
    }

    public static double parseDouble(@NotNull String doubleString) {
        return Double.parseDouble(doubleString);
    }

    private static boolean hasValue(@NotNull String string) {
        return !string.isEmpty() && !string.equals(UNKNOWN);
    }
}
