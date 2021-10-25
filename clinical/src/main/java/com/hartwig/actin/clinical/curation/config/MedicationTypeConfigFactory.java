package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class MedicationTypeConfigFactory implements CurationConfigFactory<MedicationTypeConfig> {

    @NotNull
    @Override
    public MedicationTypeConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableMedicationTypeConfig.builder().input(parts[fields.get("input")]).type(parts[fields.get("type")]).build();
    }
}
