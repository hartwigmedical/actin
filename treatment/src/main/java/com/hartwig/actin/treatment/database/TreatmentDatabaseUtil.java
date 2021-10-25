package com.hartwig.actin.treatment.database;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TreatmentDatabaseUtil {

    private static final String COHORT_SEPARATOR = ";";
    private static final String PARAMETER_SEPARATOR = ";";

    private static final String ALL_COHORTS = "all";

    private TreatmentDatabaseUtil() {
    }

    @NotNull
    public static Set<String> toCohorts(@NotNull String appliesToCohortString) {
        Set<String> cohorts = Sets.newHashSet();
        if (appliesToCohortString.isEmpty()) {
            throw new IllegalArgumentException("Missing argument appliesToCohortString");
        } else if (!appliesToCohortString.equals(ALL_COHORTS)) {
            cohorts.addAll(Sets.newHashSet(appliesToCohortString.split(COHORT_SEPARATOR)));
        }
        return cohorts;
    }

    @NotNull
    public static Set<String> toParameters(@NotNull String parameterString) {
        Set<String> parameters = Sets.newHashSet();
        if (!parameterString.isEmpty()) {
            parameters.addAll(Sets.newHashSet(parameterString.split(PARAMETER_SEPARATOR)));
        }
        return parameters;
    }
}
