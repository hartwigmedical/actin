package com.hartwig.actin.treatment.trial;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TrialConfigDatabaseUtil {

    private static final String COHORT_SEPARATOR = ";";
    private static final String CRITERION_ID_SEPARATOR = ";";
    private static final String PARAMETER_SEPARATOR = ";";

    private static final String ALL_COHORTS = "all";

    private TrialConfigDatabaseUtil() {
    }

    @NotNull
    public static Set<String> toCriterionIds(@NotNull String criterionIdsString) {
        if (criterionIdsString.isEmpty()) {
            throw new IllegalArgumentException("Missing argument criterionIdsString!");
        } else {
            return toSet(criterionIdsString, CRITERION_ID_SEPARATOR);
        }
    }

    @NotNull
    public static Set<String> toCohorts(@NotNull String appliesToCohortString) {
        if (appliesToCohortString.isEmpty()) {
            throw new IllegalArgumentException("Missing argument appliesToCohortString!");
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

    @NotNull
    public static List<String> toParameters(@NotNull String parameterString) {
        List<String> parameters = Lists.newArrayList();
        if (!parameterString.isEmpty()) {
            parameters.addAll(Lists.newArrayList(parameterString.split(PARAMETER_SEPARATOR)));
        }
        return parameters;
    }
}
