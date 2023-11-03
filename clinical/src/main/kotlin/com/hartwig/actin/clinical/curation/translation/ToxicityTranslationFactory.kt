package com.hartwig.actin.clinical.curation.translation

class ToxicityTranslationFactory : TranslationFactory<Translation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation {
        return Translation(
            input = parts[fields["toxicity"]!!],
            translated = parts[fields["translatedToxicity"]!!]
        )
    }
}