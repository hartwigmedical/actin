package com.hartwig.actin.treatment.trial.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class CohortDefinitionConfigFactory implements TrialConfigFactory<CohortDefinitionConfig> {

    private static final String CTC_COHORT_DELIMITER = ";";

    @NotNull
    @Override
    public CohortDefinitionConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableCohortDefinitionConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .cohortId(parts[fields.get("cohortId")])
                .ctcCohortIds(toSet(parts[fields.get("ctcCohortIds")]))
                .evaluable(ResourceFile.bool(parts[fields.get("evaluable")]))
                .open(ResourceFile.optionalBool(parts[fields.get("open")]))
                .slotsAvailable(ResourceFile.optionalBool(parts[fields.get("slotsAvailable")]))
                .blacklist(ResourceFile.bool(parts[fields.get("blacklist")]))
                .description(parts[fields.get("description")])
                .build();
    }

    @NotNull
    private static Set<String> toSet(@NotNull String ctcCohortIdString) {
        if (ctcCohortIdString.isEmpty()) {
            return Collections.emptySet();
        }

        return Set.of(ctcCohortIdString.split(CTC_COHORT_DELIMITER));
    }
}
