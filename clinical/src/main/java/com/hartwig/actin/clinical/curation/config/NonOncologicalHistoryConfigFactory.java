package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class NonOncologicalHistoryConfigFactory implements CurationConfigFactory<NonOncologicalHistoryConfig> {

    @NotNull
    @Override
    public NonOncologicalHistoryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);

        return ImmutableNonOncologicalHistoryConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curated(!ignore ? toCuratedObject(fields, parts) : null)
                .build();
    }

    @NotNull
    private static PriorOtherCondition toCuratedObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutablePriorOtherCondition.builder()
                .name(parts[fields.get("name")])
                .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                .category(parts[fields.get("category")])
                .build();
    }
}
