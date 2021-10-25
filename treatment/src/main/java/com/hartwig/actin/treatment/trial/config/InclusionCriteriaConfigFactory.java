package com.hartwig.actin.treatment.trial.config;

import java.util.Map;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil;

import org.jetbrains.annotations.NotNull;

public class InclusionCriteriaConfigFactory implements TrialConfigFactory<InclusionCriteriaConfig> {

    @NotNull
    @Override
    public InclusionCriteriaConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInclusionCriteriaConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .appliesToCohorts(TrialConfigDatabaseUtil.toCohorts(parts[fields.get("appliesToCohorts")]))
                .eligibilityRule(EligibilityRule.valueOf(parts[fields.get("eligibilityRule")]))
                .eligibilityParameters(TrialConfigDatabaseUtil.toParameters(parts[fields.get("eligibilityParameters")]))
                .build();
    }
}
