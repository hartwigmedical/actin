package com.hartwig.actin.treatment.database.config;

import java.util.Map;

import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class CohortConfigFactory implements TrialConfigFactory<CohortConfig> {

    @NotNull
    @Override
    public CohortConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableCohortConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .cohortId(parts[fields.get("cohortId")])
                .open(ResourceFile.bool(parts[fields.get("open")]))
                .description(parts[fields.get("description")])
                .build();
    }
}
