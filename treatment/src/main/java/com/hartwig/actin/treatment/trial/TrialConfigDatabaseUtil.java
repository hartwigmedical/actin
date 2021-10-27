package com.hartwig.actin.treatment.trial;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TrialConfigDatabaseUtil {

    private static final String COHORT_SEPARATOR = ";";
    private static final String PARAMETER_SEPARATOR = ";";

    private static final String ALL_COHORTS = "all";

    private TrialConfigDatabaseUtil() {
    }

    @NotNull
    public static Set<String> toCohorts(@NotNull String appliesToCohortString) {
        Set<String> cohorts = Sets.newHashSet();
        if (appliesToCohortString.isEmpty()) {
            throw new IllegalArgumentException("Missing argument appliesToCohortString!");
        } else if (!appliesToCohortString.equals(ALL_COHORTS)) {
            cohorts.addAll(Sets.newHashSet(appliesToCohortString.split(COHORT_SEPARATOR)));
        }
        return cohorts;
    }

    @NotNull
    public static List<String> toParameters(@NotNull String parameterString) {
        List<String> parameters = Lists.newArrayList();
        if (!parameterString.isEmpty()) {
            parameters.addAll(Lists.newArrayList(parameterString.split(PARAMETER_SEPARATOR)));
        }
        return parameters;
    }
}
