package com.hartwig.actin.treatment.database.config;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class TrialConfigFile {

    private TrialConfigFile() {
    }

    @NotNull
    public static List<TrialConfig> read(@NotNull String trialTsv) {
        List<TrialConfig> configs = Lists.newArrayList();

        return configs;
    }
}
