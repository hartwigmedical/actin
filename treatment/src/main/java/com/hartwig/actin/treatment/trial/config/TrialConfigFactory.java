package com.hartwig.actin.treatment.trial.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public interface TrialConfigFactory<T extends TrialConfig> {

    @NotNull
    T create(@NotNull Map<String, Integer> fields, @NotNull String[] parts);
}
