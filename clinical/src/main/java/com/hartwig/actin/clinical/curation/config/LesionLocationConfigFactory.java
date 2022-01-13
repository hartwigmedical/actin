package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

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
    private static LesionLocationCategory toCategory(@NotNull String category) {
        if (category.isEmpty()) {
            return null;
        }

        return LesionLocationCategory.valueOf(category.toUpperCase());
    }
}
