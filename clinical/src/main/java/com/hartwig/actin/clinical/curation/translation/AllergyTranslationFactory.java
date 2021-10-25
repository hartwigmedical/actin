package com.hartwig.actin.clinical.curation.translation;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class AllergyTranslationFactory implements TranslationFactory<AllergyTranslation> {

    @NotNull
    @Override
    public AllergyTranslation create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableAllergyTranslation.builder()
                .name(parts[fields.get("name")])
                .translatedName(parts[fields.get("translatedName")])
                .build();
    }
}
