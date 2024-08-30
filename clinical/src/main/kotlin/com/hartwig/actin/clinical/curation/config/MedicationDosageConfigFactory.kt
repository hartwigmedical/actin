package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.util.ResourceFile

class MedicationDosageConfigFactory : CurationConfigFactory<MedicationDosageConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<MedicationDosageConfig> {
        return ValidatedCurationConfig(MedicationDosageConfig(
            input = parts[fields["input"]!!],
            curated = Dosage(
                dosageMin = ResourceFile.optionalNumber(parts[fields["dosageMin"]!!]),
                dosageMax = ResourceFile.optionalNumber(parts[fields["dosageMax"]!!]),
                dosageUnit = ResourceFile.optionalString(parts[fields["dosageUnit"]!!]),
                frequency = ResourceFile.optionalNumber(parts[fields["frequency"]!!]),
                frequencyUnit = ResourceFile.optionalString(parts[fields["frequencyUnit"]!!]),
                periodBetweenValue = ResourceFile.optionalNumber(parts[fields["periodBetweenValue"]!!]),
                periodBetweenUnit = ResourceFile.optionalString(parts[fields["periodBetweenUnit"]!!]),
                ifNeeded = ResourceFile.optionalBool(parts[fields["ifNeeded"]!!])
            )
        ))
    }
}