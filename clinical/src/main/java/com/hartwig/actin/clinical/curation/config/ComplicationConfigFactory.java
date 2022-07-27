package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class ComplicationConfigFactory implements CurationConfigFactory<ComplicationConfig> {

    @NotNull
    @Override
    public ComplicationConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);

        return ImmutableComplicationConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .impliesUnknownComplicationState(ResourceFile.bool(parts[fields.get("impliesUnknownComplicationState")]))
                .curated(!ignore ? toCuratedComplication(fields, parts) : null)
                .build();
    }

    @NotNull
    private static Complication toCuratedComplication(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableComplication.builder()
                .name(parts[fields.get("name")])
                .categories(CurationUtil.toCategories(parts[fields.get("categories")]))
                .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                .month(ResourceFile.optionalInteger(parts[fields.get("month")]))
                .build();
    }
}
