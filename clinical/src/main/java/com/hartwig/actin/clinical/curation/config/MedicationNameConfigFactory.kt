package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;

import org.jetbrains.annotations.NotNull;

public class MedicationNameConfigFactory implements CurationConfigFactory<MedicationNameConfig> {

    @NotNull
    @Override
    public MedicationNameConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        String name = parts[fields.get("name")];

        return ImmutableMedicationNameConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(CurationUtil.isIgnoreString(name))
                .name(name)
                .build();
    }
}
