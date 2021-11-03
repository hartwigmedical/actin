package com.hartwig.actin.treatment.trial.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class InclusionCriteriaReferenceConfigFactory implements TrialConfigFactory<InclusionCriteriaReferenceConfig> {

    @NotNull
    @Override
    public InclusionCriteriaReferenceConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInclusionCriteriaReferenceConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .referenceId(parts[fields.get("referenceId")])
                .referenceText(parts[fields.get("referenceText")])
                .build();
    }
}
