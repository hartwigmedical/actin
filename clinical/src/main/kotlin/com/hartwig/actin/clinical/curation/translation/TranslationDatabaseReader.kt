package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation

class TranslationDatabaseReader {

    companion object {
        const val ADMINISTRATION_ROUTE_TRANSLATION_TSV = "administration_route_translation.tsv"
        const val LABORATORY_TRANSLATION_TSV = "laboratory_translation.tsv"
        const val TOXICITY_TRANSLATION_TSV = "toxicity_translation.tsv"
        const val BLOOD_TRANSFUSION_TRANSLATION_TSV = "blood_transfusion_translation.tsv"
        const val DOSAGE_UNIT_TRANSLATION_TSV = "dosage_unit_translation.tsv"
        const val SURGERY_TRANSLATION_TSV = "surgery_translation.tsv"

        fun <T> read(
            basePath: String,
            tsv: String,
            factory: TranslationFactory<Translation<T>>,
            category: CurationCategory,
            evaluatedInputFunction: (CurationExtractionEvaluation) -> Set<Translation<T>>
        ) = TranslationDatabase(readTranslations(basePath, tsv, factory).associateBy { it.input }, category, evaluatedInputFunction)

        private fun <T> readTranslations(basePath: String, tsv: String, translationFactory: TranslationFactory<T>): List<T> {
            val filePath = "$basePath/$tsv"
            val translations = TranslationFile.read(filePath, translationFactory)
            CurationDatabaseReader.LOGGER.info(" Read {} translations from {}", translations.size, filePath)
            return translations
        }
    }
}