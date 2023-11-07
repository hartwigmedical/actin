package com.hartwig.actin.clinical.curation.translation

class DosageUnitTranslationFactory : TranslationFactory<Translation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation {
        return Translation(
            input = parts[fields["dosageUnit"]!!],
            translated = parts[fields["translatedDosageUnit"]!!]
        )
    }
}