package com.hartwig.actin.treatment.trial.config;

import java.util.Map;

import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil;

import org.jetbrains.annotations.NotNull;

public class InclusionCriteriaConfigFactory implements TrialConfigFactory<InclusionCriteriaConfig> {

    @NotNull
    @Override
    public InclusionCriteriaConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInclusionCriteriaConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .criterionIds(TrialConfigDatabaseUtil.toCriterionIds(parts[fields.get("criterionIds")]))
                .appliesToCohorts(TrialConfigDatabaseUtil.toCohorts(parts[fields.get("appliesToCohorts")]))
                .inclusionRule(parts[fields.get("inclusionRule")])
                .build();
    }
}
