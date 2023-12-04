package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.Translation
import org.apache.logging.log4j.Logger

typealias InputText = String

data class CurationDatabase(
    val validationErrors: List<CurationConfigValidationError> = emptyList(),
    val configs: Map<Pair<InputText, Class<out CurationConfig>>, Set<CurationConfig>> = emptyMap(),
    val translations: Map<InputText, Translation> = emptyMap(),
    val laboratoryTranslations: Map<Pair<InputText, InputText>, LaboratoryTranslation> = emptyMap(),
) {

    operator fun plus(other: CurationDatabase?): CurationDatabase {
        if (other != null) {
            return CurationDatabase(
                validationErrors + other.validationErrors,
                configs + other.configs,

                )
        }
        return this
    }

    inline fun <reified T : CurationConfig> curate(input: InputText): Set<T> {
        return configs[input.lowercase() to T::class.java]?.map { it as T }?.toSet() ?: emptySet()
    }

    fun translate(input: String): Translation? {
        return translations[input.trim { it <= ' ' }]
    }

    fun translateLabValue(code: String, name: String): LaboratoryTranslation? {
        return laboratoryTranslations[code to name]
    }

    fun evaluate(evaluatedInputs: ExtractionEvaluation, logger: Logger) {
        listOf(
            Triple(configs, evaluatedInputs.primaryTumorEvaluatedInputs, CurationCategory.PRIMARY_TUMOR),
            Triple(
                configs, evaluatedInputs.treatmentHistoryEntryEvaluatedInputs, CurationCategory.ONCOLOGICAL_HISTORY
            ),
            Triple(configs, evaluatedInputs.secondPrimaryEvaluatedInputs, CurationCategory.SECOND_PRIMARY),
            Triple(configs, evaluatedInputs.lesionLocationEvaluatedInputs, CurationCategory.LESION_LOCATION),
            Triple(
                configs, evaluatedInputs.nonOncologicalHistoryEvaluatedInputs, CurationCategory.NON_ONCOLOGICAL_HISTORY
            ),
            Triple(configs, evaluatedInputs.ecgEvaluatedInputs, CurationCategory.ECG),
            Triple(configs, evaluatedInputs.infectionEvaluatedInputs, CurationCategory.INFECTION),
            Triple(
                configs,
                evaluatedInputs.periodBetweenUnitEvaluatedInputs,
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION
            ),
            Triple(configs, evaluatedInputs.complicationEvaluatedInputs, CurationCategory.COMPLICATION),
            Triple(configs, evaluatedInputs.toxicityEvaluatedInputs, CurationCategory.TOXICITY),
            Triple(configs, evaluatedInputs.molecularTestEvaluatedInputs, CurationCategory.MOLECULAR_TEST),
            Triple(configs, evaluatedInputs.medicationNameEvaluatedInputs, CurationCategory.MEDICATION_NAME),
            Triple(configs, evaluatedInputs.medicationDosageEvaluatedInputs, CurationCategory.MEDICATION_DOSAGE),
            Triple(configs, evaluatedInputs.intoleranceEvaluatedInputs, CurationCategory.INTOLERANCE),
        ).forEach { (configMap, evaluatedInputs, category) ->
            (configMap.keys - evaluatedInputs).forEach { input ->
                logger.warn(" Curation key '{}' not used for {} curation", input, category.categoryName)
            }
        }

        listOf(
            Triple(
                translations,
                evaluatedInputs.administrationRouteEvaluatedInputs,
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ),
            Triple(translations, evaluatedInputs.toxicityTranslationEvaluatedInputs, CurationCategory.TOXICITY_TRANSLATION),
            Triple(translations, evaluatedInputs.dosageUnitEvaluatedInputs, CurationCategory.DOSAGE_UNIT_TRANSLATION)
        ).forEach { (translationMap, evaluatedTranslations, category) ->
            (translationMap.values - evaluatedTranslations).forEach { translation ->
                logger.warn(" Curation key '{}' not used for {} translation", translation.input, category.categoryName)
            }
        }
        (laboratoryTranslations.values - evaluatedInputs.laboratoryEvaluatedInputs).forEach { translation ->
            logger.warn(
                " Curation key '{}|{}' not used for {} translation",
                translation.code,
                translation.name,
                CurationCategory.LABORATORY_TRANSLATION.categoryName
            )
        }
    }

}