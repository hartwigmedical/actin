package com.hartwig.actin.clinical.curation.translation

class AdministrationRouteTranslationFactory : TranslationFactory<AdministrationRouteTranslation> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): AdministrationRouteTranslation {
        return AdministrationRouteTranslation(
            administrationRoute = parts[fields["administrationRoute"]!!],
            translatedAdministrationRoute = parts[fields["translatedAdministrationRoute"]!!]
        )
    }
}