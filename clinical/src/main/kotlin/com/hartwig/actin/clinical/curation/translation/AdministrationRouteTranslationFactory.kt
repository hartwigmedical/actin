package com.hartwig.actin.clinical.curation.translation

class AdministrationRouteTranslationFactory : TranslationFactory<Translation<String>> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation<String> {
        return Translation(
            input = parts[fields["administrationRoute"]!!],
            translated = parts[fields["translatedAdministrationRoute"]!!]
        )
    }
}