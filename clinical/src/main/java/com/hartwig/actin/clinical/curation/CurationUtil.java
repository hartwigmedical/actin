package com.hartwig.actin.clinical.curation;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class CurationUtil {

    private static final String IGNORE = "<ignore>";
    private static final String DOID_SEPARATOR = ";";

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
    public static Set<String> toDOIDs(@NotNull String doidString) {
        if (!doidString.isEmpty()) {
            return Sets.newHashSet(doidString.split(DOID_SEPARATOR));
        } else {
            return Sets.newHashSet();
        }
    }
}
