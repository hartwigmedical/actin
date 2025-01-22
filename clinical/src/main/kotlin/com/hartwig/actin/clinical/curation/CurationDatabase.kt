package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.ValidatedCurationConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.util.left
import com.hartwig.actin.util.partitionAndJoin
import com.hartwig.actin.util.right

typealias InputText = String

class CurationDatabase<T : CurationConfig>(
    private val configs: Map<InputText, Set<T>>,
    val validationErrors: List<CurationConfigValidationError>,
    private val category: CurationCategory,
    private val evaluatedInputFunction: (CurationExtractionEvaluation) -> Set<String>
) {
    fun find(input: InputText) = configs[input.lowercase()] ?: emptySet()

    fun reportUnusedConfig(evaluations: List<CurationExtractionEvaluation>): List<UnusedCurationConfig> {
        val evaluatedInputs = evaluations.flatMap(evaluatedInputFunction)
        return configs.keys
            .filter { !evaluatedInputs.contains(it) }
            .map {
                UnusedCurationConfig(category.categoryName, it)
            }
    }

    operator fun plus(other: CurationDatabase<T>): CurationDatabase<T> {
        val (conflictingKeyErrors, combinedConfigs) = listOf(configs, other.configs).flatMap { it.entries }
            .groupBy({ it.key }, { it.value }).entries
            .map { (input, value) ->
                val filtered = if (value.size == 1) value else value.filterNot { it.all(CurationConfig::ignore) }
                if (filtered.size > 1) {
                    CurationConfigValidationError(
                        CurationCategory.COMORBIDITY.categoryName, input, "input", input, "string", "Conflicting key: $input"
                    ).left()
                } else {
                    Pair(input, filtered.firstOrNull() ?: value.first()).right()
                }
            }
            .partitionAndJoin()

        return CurationDatabase(
            combinedConfigs.toMap(),
            validationErrors + other.validationErrors + conflictingKeyErrors,
            category,
            evaluatedInputFunction
        )
    }

    companion object {
        fun <T : CurationConfig> create(
            category: CurationCategory,
            evaluationTarget: (CurationExtractionEvaluation) -> Set<String>,
            configs: List<ValidatedCurationConfig<T>>
        ): CurationDatabase<T> {
            return CurationDatabase(asInputMap(configs), configs.flatMap { it.errors }, category, evaluationTarget)
        }

        private fun <T : CurationConfig> asInputMap(configs: List<ValidatedCurationConfig<T>>) =
            configs.map { it.config }.groupBy { it.input.lowercase() }.mapValues { it.value.toSet() }
    }
}