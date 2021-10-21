package com.hartwig.actin.treatment.database.config;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class InclusionCriteriaConfigFile {

    private InclusionCriteriaConfigFile() {
    }

    @NotNull
    public static List<InclusionCriteriaConfig> read(@NotNull String inclusionCriteriaTsv) {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        return configs;
    }
}
