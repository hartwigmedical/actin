package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.datamodel.clinical.ingestion.UnusedCurationConfig
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation

class TranslationDatabase<T>(
    private val translations: Map<T, Translation<T>>,
    private val category: CurationCategory,
    private val evaluatedInputFunction: (CurationExtractionEvaluation) -> Set<Translation<T>>
) {

    fun find(input: T) = translations[input]

    fun reportUnusedTranslations(evaluation: CurationExtractionEvaluation): List<UnusedCurationConfig> {
        val evaluatedInputs = evaluatedInputFunction(evaluation).map { it.input }.toSet()
        return (translations.keys - evaluatedInputs).map { UnusedCurationConfig(category, it.toString()) }
    }
}