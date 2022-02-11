package com.hartwig.actin.treatment.trial.config;

import java.util.Map;

import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class CohortDefinitionConfigFactory implements TrialConfigFactory<CohortDefinitionConfig> {

    @NotNull
    @Override
    public CohortDefinitionConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableCohortDefinitionConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .cohortId(parts[fields.get("cohortId")])
                .open(ResourceFile.bool(parts[fields.get("open")]))
                .blacklist(ResourceFile.bool(parts[fields.get("blacklist")]))
                .description(parts[fields.get("description")])
                .build();
    }
}
