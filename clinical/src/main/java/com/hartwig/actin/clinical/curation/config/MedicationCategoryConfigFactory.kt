package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;

import org.jetbrains.annotations.NotNull;

public class MedicationCategoryConfigFactory implements CurationConfigFactory<MedicationCategoryConfig> {

    @NotNull
    @Override
    public MedicationCategoryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableMedicationCategoryConfig.builder()
                .input(parts[fields.get("input")])
                .categories(CurationUtil.toCategories(parts[fields.get("categories")]))
                .build();
    }
}
