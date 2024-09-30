package com.hartwig.actin.clinical.curation.translation

class SurgeryTranslationFactory : TranslationFactory<Translation<String>> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation<String> {
        return Translation(
            input = parts[fields["surgery"]!!],
            translated = parts[fields["translatedSurgery"]!!]
        )
    }
}