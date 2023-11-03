package com.hartwig.actin.clinical.curation.translation

class BloodTransfusionTranslationFactory : TranslationFactory<Translation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation {
        return Translation(
            input = parts[fields["product"]!!],
            translated = parts[fields["translatedProduct"]!!]
        )
    }
}