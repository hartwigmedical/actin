package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class LesionLocationConfigFactory implements CurationConfigFactory<LesionLocationConfig> {

    @NotNull
    @Override
    public LesionLocationConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableLesionLocationConfig.builder()
                .input(parts[fields.get("input")])
                .location(parts[fields.get("location")])
                .ignoreWhenOtherLesion(ResourceFile.bool(parts[fields.get("ignoreWhenOtherLesion")]))
                .build();
    }
}
