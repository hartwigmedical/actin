package com.hartwig.actin.clinical.curation.translation;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class ToxicityTranslationFactory implements TranslationFactory<ToxicityTranslation> {

    @NotNull
    @Override
    public ToxicityTranslation create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableToxicityTranslation.builder()
                .toxicity(parts[fields.get("toxicity")])
                .translatedToxicity(parts[fields.get("translatedToxicity")])
                .build();
    }
}
