package com.hartwig.actin.treatment.database.config;

import java.util.Map;

import com.hartwig.actin.treatment.database.TreatmentDatabaseUtil;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class InclusionCriteriaConfigFactory implements TrialConfigFactory<InclusionCriteriaConfig> {

    @NotNull
    @Override
    public InclusionCriteriaConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInclusionCriteriaConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .appliesToCohorts(TreatmentDatabaseUtil.toCohorts(parts[fields.get("appliesToCohorts")]))
                .eligibilityRule(EligibilityRule.valueOf(parts[fields.get("eligibilityRule")]))
                .eligibilityParameters(TreatmentDatabaseUtil.toParameters(parts[fields.get("eligibilityParameters")]))
                .build();
    }
}
