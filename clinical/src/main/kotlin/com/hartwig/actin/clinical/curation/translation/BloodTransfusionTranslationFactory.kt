package com.hartwig.actin.clinical.curation.translation

class BloodTransfusionTranslationFactory : TranslationFactory<Translation<String>> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation<String> {
        return Translation(
            input = parts[fields["product"]!!],
            translated = parts[fields["translatedProduct"]!!]
        )
    }
}