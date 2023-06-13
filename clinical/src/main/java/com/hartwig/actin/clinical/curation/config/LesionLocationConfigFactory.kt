package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LesionLocationConfigFactory implements CurationConfigFactory<LesionLocationConfig> {

    @NotNull
    @Override
    public LesionLocationConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableLesionLocationConfig.builder()
                .input(parts[fields.get("input")])
                .location(parts[fields.get("location")])
                .category(toCategory(parts[fields.get("category")]))
                .build();
    }

    @Nullable
    @VisibleForTesting
    static LesionLocationCategory toCategory(@NotNull String category) {
        if (category.isEmpty()) {
            return null;
        }

        return LesionLocationCategory.valueOf(category.replaceAll(" ", "_").toUpperCase());
    }
}
