package com.hartwig.actin.clinical.curation.translation

class LaboratoryTranslationFactory : TranslationFactory<Translation<LaboratoryIdentifiers>> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation<LaboratoryIdentifiers> {
        return Translation(
            LaboratoryIdentifiers(
                code = parts[fields["code"]!!],
                name = parts[fields["name"]!!]
            ),
            LaboratoryIdentifiers(code = parts[fields["translatedCode"]!!], name = parts[fields["translatedName"]!!])
        )
    }
}