package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning

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

        fun <T> createFromConfigs(
            configs: Set<T>,
            patientId: String,
            curationCategory: CurationCategory,
            inputText: String,
            configType: String,
            requireUniqueness: Boolean = false
        ): CurationResponse<T> {
            val notFoundWarning = if (configs.isNotEmpty()) null else CurationWarning(
                patientId = patientId,
                category = curationCategory,
                feedInput = inputText,
                message = "Could not find $configType config for input '$inputText'"
            )
            val multipleFoundWarning = if (!requireUniqueness || configs.size == 1) null else CurationWarning(
                patientId = patientId,
                category = curationCategory,
                feedInput = inputText,
                message = "Multiple $configType configs found for input '$inputText'"
            )
            return curationResponse(curationCategory, inputText, configs, setOfNotNull(notFoundWarning, multipleFoundWarning))
        }

        fun createFromTranslation(
            translated: String?,
            patientId: String,
            curationCategory: CurationCategory,
            inputText: String,
            translationType: String
        ): CurationResponse<String> {
            val warnings = if (translated != null) emptySet() else setOf(
                CurationWarning(
                    patientId = patientId,
                    category = curationCategory,
                    feedInput = inputText,
                    message = "No translation found for $translationType: '$inputText'"
                )
            )
            return curationResponse(curationCategory, inputText, setOfNotNull(translated), warnings)
        }

        private fun <T> curationResponse(
            curationCategory: CurationCategory,
            inputText: String,
            configs: Set<T>,
            warnings: Set<CurationWarning>
        ): CurationResponse<T> {
            val evaluatedInputs = setOf(inputText.lowercase())
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
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION -> ExtractionEvaluation(
                    administrationRouteEvaluatedInputs = evaluatedInputs
                )

                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION -> ExtractionEvaluation()
                CurationCategory.LABORATORY_TRANSLATION -> ExtractionEvaluation(laboratoryEvaluatedInputs = evaluatedInputs)
                CurationCategory.TOXICITY_TRANSLATION -> ExtractionEvaluation(toxicityTranslationEvaluatedInputs = evaluatedInputs)
                CurationCategory.DOSAGE_UNIT_TRANSLATION -> ExtractionEvaluation(dosageUnitEvaluatedInputs = evaluatedInputs)
            }
            return CurationResponse(configs, evaluation.copy(warnings = warnings))
        }
    }
}
