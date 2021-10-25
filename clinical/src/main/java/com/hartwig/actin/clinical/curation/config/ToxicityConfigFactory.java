package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class ToxicityConfigFactory implements CurationConfigFactory<ToxicityConfig> {

    @NotNull
    @Override
    public ToxicityConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableToxicityConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(CurationUtil.isIgnoreString(parts[fields.get("name")]))
                .name(parts[fields.get("name")])
                .grade(ResourceFile.optionalInteger(parts[fields.get("grade")]))
                .build();
    }
}
