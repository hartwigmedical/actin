package com.hartwig.actin.clinical.curation.translation

class DosageUnitTranslationFactory : TranslationFactory<DosageUnitTranslation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): DosageUnitTranslation {
        return DosageUnitTranslation(
            dosageUnit = parts[fields["dosageUnit"]!!],
            translatedDosageUnit = parts[fields["translatedDosageUnit"]!!]
        )
    }
}