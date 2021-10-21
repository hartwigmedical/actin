package com.hartwig.actin.treatment.database.config;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class CohortConfigFile {

    private CohortConfigFile() {
    }

    @NotNull
    public static List<CohortConfig> read(@NotNull String cohortTsv) {
        List<CohortConfig> configs = Lists.newArrayList();

        return configs;
    }
}
