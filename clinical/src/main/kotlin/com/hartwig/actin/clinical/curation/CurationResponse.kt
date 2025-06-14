package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning

data class CurationResponse<T>(
    val configs: Set<T> = emptySet(), val extractionEvaluation: CurationExtractionEvaluation = CurationExtractionEvaluation()
) {

    fun config(): T? {
        return configs.firstOrNull()
    }

    operator fun plus(other: CurationResponse<T>): CurationResponse<T> {
        return CurationResponse(
            configs = configs + other.configs,
            extractionEvaluation = extractionEvaluation + other.extractionEvaluation
        )
    }

    companion object {
        fun <T : CurationConfig> createFromConfigs(
            configs: Set<T>,
            patientId: String,
            curationCategory: CurationCategory,
            inputText: String,
            configType: String,
            requireUniqueness: Boolean = false
        ): CurationResponse<T> {
            val notFoundWarning = if (configs.isNotEmpty() || (requireUniqueness && inputText.isEmpty())) null else CurationWarning(
                patientId = patientId,
                category = curationCategory,
                feedInput = inputText,
                message = "Could not find $configType config for input '$inputText'"
            )
            val multipleFoundWarning = if (!requireUniqueness || configs.size <= 1) null else CurationWarning(
                patientId = patientId,
                category = curationCategory,
                feedInput = inputText,
                message = "Multiple $configType configs found for input '$inputText'"
            )
            return create(
                curationCategory,
                inputText.lowercase(),
                configs,
                setOfNotNull(notFoundWarning, multipleFoundWarning)
            )
        }

        fun createFromTranslation(
            translation: Translation<String>?,
            patientId: String,
            curationCategory: CurationCategory,
            inputText: String,
            translationType: String
        ): CurationResponse<Translation<String>> {
            val foundTranslations = setOfNotNull(translation)
            val warnings = if (translation != null) emptySet() else setOf(
                CurationWarning(
                    patientId = patientId,
                    category = curationCategory,
                    feedInput = inputText,
                    message = "No translation found for $translationType: '$inputText'"
                )
            )

            val evaluation = when (curationCategory) {
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION -> CurationExtractionEvaluation(administrationRouteEvaluatedInputs = foundTranslations)
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION -> CurationExtractionEvaluation()
                CurationCategory.TOXICITY_TRANSLATION -> CurationExtractionEvaluation(toxicityTranslationEvaluatedInputs = foundTranslations)
                CurationCategory.DOSAGE_UNIT_TRANSLATION -> CurationExtractionEvaluation(dosageUnitEvaluatedInputs = foundTranslations)
                else -> throw IllegalStateException("Unsupported curation category for translation lookup: $curationCategory")
            }
            return CurationResponse(foundTranslations, evaluation.copy(warnings = warnings))
        }

        fun <T : CurationConfig> create(
            curationCategory: CurationCategory,
            inputText: String,
            configs: Set<T>,
            warnings: Set<CurationWarning>
        ): CurationResponse<T> {
            val evaluatedInputs = setOf(inputText)
            val evaluation = when (curationCategory) {
                CurationCategory.PRIMARY_TUMOR -> CurationExtractionEvaluation(primaryTumorEvaluatedInputs = evaluatedInputs)
                CurationCategory.ONCOLOGICAL_HISTORY -> CurationExtractionEvaluation(treatmentHistoryEntryEvaluatedInputs = evaluatedInputs)
                CurationCategory.PRIOR_PRIMARY -> CurationExtractionEvaluation(priorPrimaryEvaluatedInputs = evaluatedInputs)
                CurationCategory.LESION_LOCATION -> CurationExtractionEvaluation(lesionLocationEvaluatedInputs = evaluatedInputs)
                CurationCategory.NON_ONCOLOGICAL_HISTORY -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.ECG -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.INFECTION -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION -> CurationExtractionEvaluation(
                    periodBetweenUnitEvaluatedInputs = evaluatedInputs
                )

                CurationCategory.COMORBIDITY -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.COMPLICATION -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.TOXICITY -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.MOLECULAR_TEST_IHC -> CurationExtractionEvaluation(molecularTestEvaluatedInputs = evaluatedInputs)
                CurationCategory.SEQUENCING_TEST -> CurationExtractionEvaluation(sequencingTestEvaluatedInputs = evaluatedInputs)
                CurationCategory.SEQUENCING_TEST_RESULT -> CurationExtractionEvaluation(sequencingTestResultEvaluatedInputs = evaluatedInputs)
                CurationCategory.MEDICATION_NAME -> CurationExtractionEvaluation(medicationNameEvaluatedInputs = evaluatedInputs)
                CurationCategory.MEDICATION_DOSAGE -> CurationExtractionEvaluation(medicationDosageEvaluatedInputs = evaluatedInputs)
                CurationCategory.INTOLERANCE -> CurationExtractionEvaluation(comorbidityEvaluatedInputs = evaluatedInputs)
                CurationCategory.SURGERY_NAME -> CurationExtractionEvaluation(surgeryCurationEvaluatedInputs = evaluatedInputs)
                CurationCategory.LAB_MEASUREMENT -> CurationExtractionEvaluation(labMeasurementEvaluatedInputs = evaluatedInputs)
                else -> throw IllegalStateException("Unsupported curation category for config lookup: $curationCategory")
            }
            return CurationResponse(
                configs,
                evaluation.copy(warnings = warnings)
            )
        }
    }
}
