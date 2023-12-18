package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation

class TranslationDatabase<T>(
    private val translations: Map<T, Translation<T>>,
    private val category: CurationCategory,
    private val evaluatedInputFunction: (ExtractionEvaluation) -> Set<Translation<T>>
) {
    fun find(input: T) = translations[input]

    fun reportUnusedTranslations(evaluations: List<ExtractionEvaluation>): List<UnusedCurationConfig> {
        val evaluatedInputs = evaluations.flatMap(evaluatedInputFunction).map { it.input }
        return translations.values.filter { !evaluatedInputs.contains(it.input) }
            .map {
                UnusedCurationConfig(category, it.input.toString())
            }
    }
}