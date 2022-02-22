package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class CancerRelatedComplicationConfigFactory implements CurationConfigFactory<CancerRelatedComplicationConfig> {

    @NotNull
    @Override
    public CancerRelatedComplicationConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableCancerRelatedComplicationConfig.builder()
                .input(parts[fields.get("input")]).name(parts[fields.get("name")])
                .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                .month(ResourceFile.optionalInteger(parts[fields.get("month")]))
                .build();

    }
}
