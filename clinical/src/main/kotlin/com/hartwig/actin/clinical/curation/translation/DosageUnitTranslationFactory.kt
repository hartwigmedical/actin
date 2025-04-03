package com.hartwig.actin.clinical.curation.translation

class DosageUnitTranslationFactory : TranslationFactory<Translation<String>> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation<String> {
        return Translation(
            input = parts[fields["dosageUnit"]!!],
            translated = parts[fields["translatedDosageUnit"]!!]
        )
    }
}