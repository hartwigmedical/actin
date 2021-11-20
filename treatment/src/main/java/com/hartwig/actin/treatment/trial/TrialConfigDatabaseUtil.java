package com.hartwig.actin.treatment.trial;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TrialConfigDatabaseUtil {

    private static final String COHORT_SEPARATOR = ";";
    private static final String REFERENCE_ID_SEPARATOR = ",";

    private static final String ALL_COHORTS = "all";

    private TrialConfigDatabaseUtil() {
    }

    @NotNull
    public static Set<String> toReferenceIds(@NotNull String referenceIdsString) {
        if (referenceIdsString.isEmpty()) {
            throw new IllegalArgumentException("Empty argument referenceIdsString!");
        } else {
            return toSet(referenceIdsString, REFERENCE_ID_SEPARATOR);
        }
    }

    @NotNull
    public static Set<String> toCohorts(@NotNull String appliesToCohortString) {
        if (appliesToCohortString.isEmpty()) {
            throw new IllegalArgumentException("Empty argument appliesToCohortString!");
        } else if (!appliesToCohortString.equals(ALL_COHORTS)) {
            return toSet(appliesToCohortString, COHORT_SEPARATOR);
        } else {
            return Sets.newHashSet();
        }
    }

    @NotNull
    private static Set<String> toSet(@NotNull String string, @NotNull String delimiter) {
        Set<String> set = Sets.newHashSet();
        for (String part : string.split(delimiter)) {
            set.add(part.trim());
        }
        return set;
    }
}
