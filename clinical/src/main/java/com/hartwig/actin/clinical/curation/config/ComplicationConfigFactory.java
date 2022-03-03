package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class ComplicationConfigFactory implements CurationConfigFactory<ComplicationConfig> {

    @NotNull
    @Override
    public ComplicationConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        Complication curated = ImmutableComplication.builder()
                .name(parts[fields.get("name")])
                .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                .month(ResourceFile.optionalInteger(parts[fields.get("month")]))
                .build();

        return ImmutableComplicationConfig.builder().input(parts[fields.get("input")]).curated(curated).build();
    }
}
