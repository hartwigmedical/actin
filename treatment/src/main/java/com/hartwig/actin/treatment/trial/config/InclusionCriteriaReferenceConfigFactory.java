package com.hartwig.actin.treatment.trial.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class InclusionCriteriaReferenceConfigFactory implements TrialConfigFactory<InclusionCriteriaReferenceConfig> {

    @NotNull
    @Override
    public InclusionCriteriaReferenceConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInclusionCriteriaReferenceConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .criterionId(parts[fields.get("criterionId")])
                .criterionText(parts[fields.get("criterionText")])
                .build();
    }
}
