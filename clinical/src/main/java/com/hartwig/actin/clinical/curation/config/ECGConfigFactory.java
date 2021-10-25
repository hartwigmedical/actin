package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class ECGConfigFactory implements CurationConfigFactory<ECGConfig> {

    @NotNull
    @Override
    public ECGConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableECGConfig.builder().input(parts[fields.get("input")]).interpretation(parts[fields.get("interpretation")]).build();
    }
}
