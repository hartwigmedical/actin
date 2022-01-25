package com.hartwig.actin.treatment.trial.config;

import java.util.Map;
import java.util.StringJoiner;

import org.jetbrains.annotations.NotNull;

public class InclusionCriteriaReferenceConfigFactory implements TrialConfigFactory<InclusionCriteriaReferenceConfig> {

    @NotNull
    @Override
    public InclusionCriteriaReferenceConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid criteria reference config provided. Possibly due to hard line breaks? ('" + toString(parts) + "')");
        }

        return ImmutableInclusionCriteriaReferenceConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .referenceId(parts[fields.get("referenceId")])
                .referenceText(parts[fields.get("referenceText")])
                .build();
    }

    @NotNull
    private static String toString(@NotNull String[] parts) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String part : parts) {
            joiner.add(part);
        }
        return joiner.toString().trim();
    }
}
