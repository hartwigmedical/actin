package com.hartwig.actin.clinical.curation.translation;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public interface TranslationFactory<T extends Translation> {

    @NotNull
    T create(@NotNull Map<String, Integer> fields, @NotNull String[] parts);
}
