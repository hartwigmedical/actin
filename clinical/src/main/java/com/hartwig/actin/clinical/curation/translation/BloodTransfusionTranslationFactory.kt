package com.hartwig.actin.clinical.curation.translation

class BloodTransfusionTranslationFactory : TranslationFactory<BloodTransfusionTranslation> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String?>): BloodTransfusionTranslation {
        return ImmutableBloodTransfusionTranslation.builder()
            .product(parts[fields["product"]!!])
            .translatedProduct(parts[fields["translatedProduct"]!!])
            .build()
    }
}