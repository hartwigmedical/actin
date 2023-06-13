package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class MedicationDosageConfigFactory implements CurationConfigFactory<MedicationDosageConfig> {

    @NotNull
    @Override
    public MedicationDosageConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableMedicationDosageConfig.builder()
                .input(parts[fields.get("input")])
                .dosageMin(ResourceFile.optionalNumber(parts[fields.get("dosageMin")]))
                .dosageMax(ResourceFile.optionalNumber(parts[fields.get("dosageMax")]))
                .dosageUnit(ResourceFile.optionalString(parts[fields.get("dosageUnit")]))
                .frequency(ResourceFile.optionalNumber(parts[fields.get("frequency")]))
                .frequencyUnit(ResourceFile.optionalString(parts[fields.get("frequencyUnit")]))
                .ifNeeded(ResourceFile.optionalBool(parts[fields.get("ifNeeded")]))
                .build();
    }
}
