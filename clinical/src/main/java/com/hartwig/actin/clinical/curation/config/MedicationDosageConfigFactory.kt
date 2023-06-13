package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.util.ResourceFile

class MedicationDosageConfigFactory : CurationConfigFactory<MedicationDosageConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): MedicationDosageConfig {
        return ImmutableMedicationDosageConfig.builder()
            .input(parts[fields["input"]!!])
            .dosageMin(ResourceFile.optionalNumber(parts[fields["dosageMin"]!!]))
            .dosageMax(ResourceFile.optionalNumber(parts[fields["dosageMax"]!!]))
            .dosageUnit(ResourceFile.optionalString(parts[fields["dosageUnit"]!!]))
            .frequency(ResourceFile.optionalNumber(parts[fields["frequency"]!!]))
            .frequencyUnit(ResourceFile.optionalString(parts[fields["frequencyUnit"]!!]))
            .ifNeeded(ResourceFile.optionalBool(parts[fields["ifNeeded"]!!]))
            .build()
    }
}