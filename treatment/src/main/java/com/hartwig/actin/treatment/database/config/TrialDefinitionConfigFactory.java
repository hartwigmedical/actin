package com.hartwig.actin.treatment.database.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class TrialDefinitionConfigFactory implements TrialConfigFactory<TrialDefinitionConfig> {

    @NotNull
    @Override
    public TrialDefinitionConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableTrialDefinitionConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .acronym(parts[fields.get("acronym")])
                .title(parts[fields.get("title")])
                .build();
    }
}