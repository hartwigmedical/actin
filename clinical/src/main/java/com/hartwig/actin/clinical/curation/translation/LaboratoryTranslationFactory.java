package com.hartwig.actin.clinical.curation.translation;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class LaboratoryTranslationFactory implements TranslationFactory<LaboratoryTranslation> {

    @NotNull
    @Override
    public LaboratoryTranslation create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableLaboratoryTranslation.builder()
                .code(parts[fields.get("code")])
                .translatedCode(parts[fields.get("translatedCode")])
                .name(parts[fields.get("name")])
                .translatedName(parts[fields.get("translatedName")])
                .build();
    }
}
