package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

interface CurationConfigFactory<T extends CurationConfig> {

    @NotNull
    T create(@NotNull Map<String, Integer> fields, @NotNull String[] parts);
}
