package com.hartwig.actin.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResourceFile {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String UNKNOWN = "unknown";

    private ResourceFile() {
    }

    @Nullable
    public static String optionalString(@NotNull String string) {
        return hasValue(string) ? string : null;
    }

    @Nullable
    public static Boolean optionalBool(@NotNull String bool) {
        return hasValue(bool) ? bool(bool) : null;
    }

    public static boolean bool(@NotNull String bool) {
        if (bool.equals("1")) {
            return true;
        } else if (bool.equals("0")) {
            return false;
        } else {
            throw new IllegalArgumentException("Cannot convert curation value to boolean: '" + bool + "'");
        }
    }

    @Nullable
    public static LocalDate optionalDate(@NotNull String date) {
        return hasValue(date) ? date(date) : null;
    }

    @NotNull
    public static LocalDate date(@NotNull String date) {
        return LocalDate.parse(date, DATE_FORMAT);
    }

    @Nullable
    public static Integer optionalInteger(@NotNull String integer) {
        return hasValue(integer) ? integer(integer) : null;
    }

    public static int integer(@NotNull String integer) {
        return Integer.parseInt(integer);
    }

    @Nullable
    public static Double optionalNumber(@NotNull String doubleString) {
        return hasValue(doubleString) ? number(doubleString) : null;
    }

    public static double number(@NotNull String doubleString) {
        return Double.parseDouble(doubleString);
    }

    private static boolean hasValue(@NotNull String string) {
        return !string.isEmpty() && !string.equals(UNKNOWN);
    }
}
