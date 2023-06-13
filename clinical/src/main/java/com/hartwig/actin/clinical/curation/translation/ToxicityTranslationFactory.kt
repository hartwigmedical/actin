package com.hartwig.actin.clinical.curation.translation

class ToxicityTranslationFactory : TranslationFactory<ToxicityTranslation> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String?>): ToxicityTranslation {
        return ImmutableToxicityTranslation.builder()
            .toxicity(parts[fields["toxicity"]!!])
            .translatedToxicity(parts[fields["translatedToxicity"]!!])
            .build()
    }
}