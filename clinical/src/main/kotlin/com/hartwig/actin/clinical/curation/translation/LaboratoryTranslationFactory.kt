package com.hartwig.actin.clinical.curation.translation

class LaboratoryTranslationFactory : TranslationFactory<LaboratoryTranslation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): LaboratoryTranslation {
        return LaboratoryTranslation(
            code = parts[fields["code"]!!],
            translatedCode = parts[fields["translatedCode"]!!],
            name = parts[fields["name"]!!],
            translatedName = parts[fields["translatedName"]!!]
        )
    }
}