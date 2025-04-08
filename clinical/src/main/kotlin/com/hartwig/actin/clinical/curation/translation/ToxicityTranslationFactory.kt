package com.hartwig.actin.clinical.curation.translation

class ToxicityTranslationFactory : TranslationFactory<Translation<String>> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation<String> {
        return Translation(
            input = parts[fields["toxicity"]!!],
            translated = parts[fields["translatedToxicity"]!!]
        )
    }
}