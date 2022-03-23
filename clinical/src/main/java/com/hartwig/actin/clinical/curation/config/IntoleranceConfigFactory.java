package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;

import org.jetbrains.annotations.NotNull;

public class IntoleranceConfigFactory implements CurationConfigFactory<IntoleranceConfig> {

    @NotNull
    @Override
    public IntoleranceConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableIntoleranceConfig.builder()
                .input(parts[fields.get("input")])
                .name(parts[fields.get("name")])
                .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                .build();
    }
}
