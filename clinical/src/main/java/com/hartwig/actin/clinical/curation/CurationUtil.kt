package com.hartwig.actin.clinical.curation;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class CurationUtil {

    private static final String IGNORE = "<ignore>";
    private static final String DOID_DELIMITER = ";";
    private static final String CATEGORIES_DELIMITER = ";";

    private CurationUtil() {
    }

    public static boolean isIgnoreString(@NotNull String input) {
        return input.equals(IGNORE);
    }

    @NotNull
    public static String capitalizeFirstLetterOnly(@NotNull String string) {
        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    @NotNull
    public static String fullTrim(@NotNull String input) {
        String reformatted = input.trim();
        while (reformatted.contains("  ")) {
            reformatted = reformatted.replaceAll(" {2}", " ");
        }
        return reformatted;
    }

    @NotNull
    public static Set<String> toDOIDs(@NotNull String doidString) {
        return toSet(doidString, DOID_DELIMITER);
    }

    @NotNull
    public static Set<String> toCategories(@NotNull String categoriesString) {
        return toSet(categoriesString, CATEGORIES_DELIMITER);
    }

    @NotNull
    private static Set<String> toSet(@NotNull String setString, @NotNull String delimiter) {
        if (setString.isEmpty()) {
            return Sets.newHashSet();
        }

        Set<String> strings = Sets.newHashSet();
        for (String string : setString.split(delimiter)) {
            strings.add(string.trim());
        }
        return strings;
    }
}
