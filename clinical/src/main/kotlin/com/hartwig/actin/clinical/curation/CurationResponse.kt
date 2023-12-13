package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.Translation

data class CurationResponse<T>(
    val configs: Set<T> = emptySet(), val extractionEvaluation: ExtractionEvaluation = ExtractionEvaluation()
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
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION -> {
                    ExtractionEvaluation(administrationRouteEvaluatedInputs = foundTranslations)
                }

                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION -> ExtractionEvaluation()
                CurationCategory.TOXICITY_TRANSLATION -> ExtractionEvaluation(toxicityTranslationEvaluatedInputs = foundTranslations)
                CurationCategory.DOSAGE_UNIT_TRANSLATION -> ExtractionEvaluation(dosageUnitEvaluatedInputs = foundTranslations)
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
                CurationCategory.PRIMARY_TUMOR -> ExtractionEvaluation(primaryTumorEvaluatedInputs = evaluatedInputs)
                CurationCategory.ONCOLOGICAL_HISTORY -> ExtractionEvaluation(treatmentHistoryEntryEvaluatedInputs = evaluatedInputs)
                CurationCategory.SECOND_PRIMARY -> ExtractionEvaluation(secondPrimaryEvaluatedInputs = evaluatedInputs)
                CurationCategory.LESION_LOCATION -> ExtractionEvaluation(lesionLocationEvaluatedInputs = evaluatedInputs)
                CurationCategory.NON_ONCOLOGICAL_HISTORY -> ExtractionEvaluation(nonOncologicalHistoryEvaluatedInputs = evaluatedInputs)
                CurationCategory.ECG -> ExtractionEvaluation(ecgEvaluatedInputs = evaluatedInputs)
                CurationCategory.INFECTION -> ExtractionEvaluation(infectionEvaluatedInputs = evaluatedInputs)
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION -> ExtractionEvaluation(
                    periodBetweenUnitEvaluatedInputs = evaluatedInputs
                )

                CurationCategory.COMPLICATION -> ExtractionEvaluation(complicationEvaluatedInputs = evaluatedInputs)
                CurationCategory.TOXICITY -> ExtractionEvaluation(toxicityEvaluatedInputs = evaluatedInputs)
                CurationCategory.MOLECULAR_TEST -> ExtractionEvaluation(molecularTestEvaluatedInputs = evaluatedInputs)
                CurationCategory.MEDICATION_NAME -> ExtractionEvaluation(medicationNameEvaluatedInputs = evaluatedInputs)
                CurationCategory.MEDICATION_DOSAGE -> ExtractionEvaluation(medicationDosageEvaluatedInputs = evaluatedInputs)
                CurationCategory.INTOLERANCE -> ExtractionEvaluation(intoleranceEvaluatedInputs = evaluatedInputs)
                CurationCategory.CYP_INTERACTION -> ExtractionEvaluation()
                CurationCategory.QT_PROLONGATION -> ExtractionEvaluation()
                else -> throw IllegalStateException("Unsupported curation category for config lookup: $curationCategory")
            }
            return CurationResponse(
                configs.map { it }.toSet(),
                evaluation.copy(warnings = warnings)
            )
        }
    }
}
