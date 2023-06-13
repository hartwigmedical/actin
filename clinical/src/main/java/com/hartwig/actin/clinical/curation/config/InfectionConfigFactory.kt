package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class InfectionConfigFactory implements CurationConfigFactory<InfectionConfig> {

    @NotNull
    @Override
    public InfectionConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInfectionConfig.builder()
                .input(parts[fields.get("input")])
                .interpretation(parts[fields.get("interpretation")])
                .build();
    }
}
