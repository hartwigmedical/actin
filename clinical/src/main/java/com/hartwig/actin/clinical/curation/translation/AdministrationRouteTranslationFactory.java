package com.hartwig.actin.clinical.curation.translation;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class AdministrationRouteTranslationFactory implements TranslationFactory<AdministrationRouteTranslation> {

    @NotNull
    @Override
    public AdministrationRouteTranslation create(@NotNull final Map<String, Integer> fields, @NotNull final String[] parts) {
        return ImmutableAdministrationRouteTranslation.builder()
                .administrationRoute(parts[fields.get("administrationRoute")])
                .translatedAdministrationRoute(parts[fields.get("translatedAdministrationRoute")])
                .build();
    }
}
