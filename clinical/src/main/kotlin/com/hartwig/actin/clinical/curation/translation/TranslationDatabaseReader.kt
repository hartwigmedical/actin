package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.clinical.curation.CurationDatabaseReader

class TranslationDatabaseReader(private val curationDirectory: String) {

    fun bloodTransfusions() =
        readTranslationsDatabase(curationDirectory, BLOOD_TRANSFUSION_TRANSLATION_TSV, BloodTransfusionTranslationFactory())

    fun administrationRoute() =
        readTranslationsDatabase(curationDirectory, ADMINISTRATION_ROUTE_TRANSLATION_TSV, AdministrationRouteTranslationFactory())

    fun toxicity() =
        readTranslationsDatabase(curationDirectory, TOXICITY_TRANSLATION_TSV, ToxicityTranslationFactory())

    fun dosageUnit() =
        readTranslationsDatabase(curationDirectory, DOSAGE_UNIT_TRANSLATION_TSV, DosageUnitTranslationFactory())

    fun labratoryTranslation() =
        readTranslationsDatabase(curationDirectory, LABORATORY_TRANSLATION_TSV, LaboratoryTranslationFactory())

    companion object {
        const val ADMINISTRATION_ROUTE_TRANSLATION_TSV = "administration_route_translation.tsv"
        const val LABORATORY_TRANSLATION_TSV = "laboratory_translation.tsv"
        const val TOXICITY_TRANSLATION_TSV = "toxicity_translation.tsv"
        const val BLOOD_TRANSFUSION_TRANSLATION_TSV = "blood_transfusion_translation.tsv"
        const val DOSAGE_UNIT_TRANSLATION_TSV = "dosage_unit_translation.tsv"

        fun <T> readTranslationsDatabase(
            basePath: String, tsv: String, factory: TranslationFactory<Translation<T>>
        ) = TranslationDatabase(readTranslations(basePath, tsv, factory).associateBy { it.input })

        private fun <T> readTranslations(basePath: String, tsv: String, translationFactory: TranslationFactory<T>): List<T> {
            val filePath = basePath + tsv
            val translations = TranslationFile.read(filePath, translationFactory)
            CurationDatabaseReader.LOGGER.info(" Read {} translations from {}", translations.size, filePath)
            return translations
        }
    }
}