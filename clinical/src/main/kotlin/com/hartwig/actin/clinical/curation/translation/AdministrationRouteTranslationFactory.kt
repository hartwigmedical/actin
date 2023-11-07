package com.hartwig.actin.clinical.curation.translation

class AdministrationRouteTranslationFactory : TranslationFactory<Translation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): Translation {
        return Translation(
            input = parts[fields["administrationRoute"]!!],
            translated = parts[fields["translatedAdministrationRoute"]!!]
        )
    }
}