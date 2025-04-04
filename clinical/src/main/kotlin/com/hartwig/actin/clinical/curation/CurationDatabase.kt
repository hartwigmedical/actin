package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.ValidatedCurationConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.clinical.ingestion.UnusedCurationConfig

typealias InputText = String

class CurationDatabase<T : CurationConfig>(
    private val configs: Map<InputText, Set<T>>,
    val validationErrors: List<CurationConfigValidationError>,
    private val category: CurationCategory,
    private val evaluatedInputFunction: (CurationExtractionEvaluation) -> Set<String>
) {

    fun find(input: InputText) = configs[input.lowercase()] ?: emptySet()

    fun reportUnusedConfig(evaluation: CurationExtractionEvaluation): List<UnusedCurationConfig> {
        return (configs.keys - evaluatedInputFunction(evaluation)).map { UnusedCurationConfig(category, it) }
    }

    operator fun plus(other: CurationDatabase<T>): CurationDatabase<T> {
        val combinedConfigs = listOf(configs, other.configs).flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, value) ->
                val filtered = value.filterNot { it.all(CurationConfig::ignore) }
                if (filtered.isEmpty()) value.first() else filtered.reduce(Set<T>::plus)
            }

        return CurationDatabase(
            combinedConfigs,
            validationErrors + other.validationErrors,
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