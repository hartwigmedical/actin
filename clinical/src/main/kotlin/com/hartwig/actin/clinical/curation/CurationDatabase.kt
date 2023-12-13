package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.ValidatedCurationConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation

typealias InputText = String

class CurationDatabase<T : CurationConfig>(
    private val configs: Map<InputText, Set<T>>,
    private val validationErrors: List<CurationConfigValidationError>,
    private val category: CurationCategory,
    private val evaluatedInputFunction: (ExtractionEvaluation) -> Set<String>
) {
    fun curate(input: InputText) = configs[input.lowercase()] ?: emptySet()

    fun validate(evaluations: List<ExtractionEvaluation>): List<CurationConfigValidationError> {
        val evaluatedInputs = evaluations.flatMap(evaluatedInputFunction)
        return configs.values.flatten()
            .filter { !evaluatedInputs.contains(it.input) }
            .map { CurationConfigValidationError("Curation key '${it.input}' not used for '${category}' curation") } + validationErrors
    }

    companion object {
        fun <T : CurationConfig> create(
            category: CurationCategory,
            evaluationTarget: (ExtractionEvaluation) -> Set<String>,
            configs: List<ValidatedCurationConfig<T>>
        ): CurationDatabase<T> {
            return CurationDatabase(asInputMap(configs), configs.flatMap { it.errors }, category, evaluationTarget)
        }

        private fun <T : CurationConfig> asInputMap(configs: List<ValidatedCurationConfig<T>>) =
            configs.map { it.config }.groupBy { it.input.lowercase() }.mapValues { it.value.toSet() }
    }
}