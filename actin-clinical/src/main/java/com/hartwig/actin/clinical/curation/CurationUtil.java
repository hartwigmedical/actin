package com.hartwig.actin.clinical.curation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CurationUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String DOID_SEPARATOR = ";";

    private CurationUtil() {
    }

    @Nullable
    public static String capitalizeFirstLetter(@Nullable String string) {
        if (string == null) {
            return null;
        }

        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    @Nullable
    public static String optionalString(@NotNull String string) {
        return !string.isEmpty() ? string : null;
    }

    @NotNull
    public static Set<String> parseDOID(@NotNull String doidString) {
        if (!doidString.isEmpty()) {
            return Sets.newHashSet(doidString.split(DOID_SEPARATOR));
        } else {
            return Sets.newHashSet();
        }
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
        return !date.isEmpty() ? parseDate(date) : null;
    }

    @NotNull
    public static LocalDate parseDate(@NotNull String date) {
        return LocalDate.parse(date, DATE_FORMAT);
    }

    @Nullable
    public static Integer parseOptionalInteger(@NotNull String integer) {
        return !integer.isEmpty() ? parseInteger(integer) : null;
    }

    public static int parseInteger(@NotNull String integer) {
        return Integer.parseInt(integer);
    }
}