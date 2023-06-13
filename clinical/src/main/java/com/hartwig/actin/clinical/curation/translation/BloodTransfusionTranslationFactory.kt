package com.hartwig.actin.clinical.curation.translation;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class BloodTransfusionTranslationFactory implements TranslationFactory<BloodTransfusionTranslation> {

    @NotNull
    @Override
    public BloodTransfusionTranslation create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableBloodTransfusionTranslation.builder()
                .product(parts[fields.get("product")])
                .translatedProduct(parts[fields.get("translatedProduct")])
                .build();
    }
}
