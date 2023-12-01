package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidatedResponse
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.Translation
import org.apache.logging.log4j.Logger

typealias InputText = String

data class CurationDatabase(
    val validationErrors: List<CurationConfigValidationError> = emptyList(),
    val primaryTumorConfigs: Map<InputText, Set<PrimaryTumorConfig>> = emptyMap(),
    val treatmentHistoryEntryConfigs: Map<InputText, Set<TreatmentHistoryEntryConfig>> = emptyMap(),
    val secondPrimaryConfigs: Map<InputText, Set<SecondPrimaryConfig>> = emptyMap(),
    val lesionLocationConfigs: Map<InputText, Set<LesionLocationConfig>> = emptyMap(),
    val nonOncologicalHistoryConfigs: Map<InputText, Set<NonOncologicalHistoryConfig>> = emptyMap(),
    val ecgConfigs: Map<InputText, Set<ECGConfig>> = emptyMap(),
    val infectionConfigs: Map<InputText, Set<InfectionConfig>> = emptyMap(),
    val periodBetweenUnitConfigs: Map<InputText, Set<PeriodBetweenUnitConfig>> = emptyMap(),
    val complicationConfigs: Map<InputText, Set<ComplicationConfig>> = emptyMap(),
    val toxicityConfigs: Map<InputText, Set<ToxicityConfig>> = emptyMap(),
    val molecularTestConfigs: Map<InputText, Set<MolecularTestConfig>> = emptyMap(),
    val medicationNameConfigs: Map<InputText, Set<MedicationNameConfig>> = emptyMap(),
    val medicationDosageConfigs: Map<InputText, Set<MedicationDosageConfig>> = emptyMap(),
    val intoleranceConfigs: Map<InputText, Set<IntoleranceConfig>> = emptyMap(),
    val cypInteractionConfigs: Map<InputText, Set<CypInteractionConfig>> = emptyMap(),
    val qtProlongingConfigs: Map<InputText, Set<QTProlongatingConfig>> = emptyMap(),
    val administrationRouteTranslations: Map<InputText, Translation> = emptyMap(),
    val laboratoryTranslations: Map<Pair<InputText, InputText>, LaboratoryTranslation> = emptyMap(),
    val toxicityTranslations: Map<InputText, Translation> = emptyMap(),
    val bloodTransfusionTranslations: Map<InputText, Translation> = emptyMap(),
    val dosageUnitTranslations: Map<InputText, Translation> = emptyMap()
) {

    operator fun plus(other: CurationDatabase?): CurationDatabase {
        if (other != null) {
            return CurationDatabase(
                validationErrors + other.validationErrors,
                primaryTumorConfigs + other.primaryTumorConfigs,
                treatmentHistoryEntryConfigs + other.treatmentHistoryEntryConfigs,
                secondPrimaryConfigs + other.secondPrimaryConfigs,
                lesionLocationConfigs + other.lesionLocationConfigs,
                nonOncologicalHistoryConfigs + other.nonOncologicalHistoryConfigs,
                ecgConfigs + other.ecgConfigs,
                infectionConfigs + other.infectionConfigs,
                periodBetweenUnitConfigs + other.periodBetweenUnitConfigs,
                complicationConfigs + other.complicationConfigs,
                toxicityConfigs + other.toxicityConfigs,
                molecularTestConfigs + other.molecularTestConfigs,
                medicationNameConfigs + other.medicationNameConfigs,
                medicationDosageConfigs + other.medicationDosageConfigs,
                intoleranceConfigs + other.intoleranceConfigs,
                cypInteractionConfigs + other.cypInteractionConfigs,
                qtProlongingConfigs + other.qtProlongingConfigs,
                administrationRouteTranslations + other.administrationRouteTranslations,
                laboratoryTranslations + other.laboratoryTranslations,
                toxicityTranslations + other.toxicityTranslations,
                bloodTransfusionTranslations + other.bloodTransfusionTranslations,
                dosageUnitTranslations + other.dosageUnitTranslations

            )
        }
        return this
    }

    fun findLesionConfigs(lesion: String): Set<LesionLocationConfig> {
        return find(lesionLocationConfigs, lesion)
    }

    fun findTumorConfigs(tumorInput: String): Set<PrimaryTumorConfig> {
        return find(primaryTumorConfigs, tumorInput)
    }

    fun findECGConfig(input: String): Set<ECGConfig> {
        return find(ecgConfigs, input)
    }

    fun findInfectionStatusConfig(input: String): Set<InfectionConfig> {
        return find(infectionConfigs, input)
    }

    fun findNonOncologicalHistoryConfigs(input: String): Set<NonOncologicalHistoryConfig> {
        return find(nonOncologicalHistoryConfigs, input)
    }

    fun findTreatmentHistoryEntryConfigs(input: String): Set<TreatmentHistoryEntryConfig> {
        return find(treatmentHistoryEntryConfigs, input)
    }

    fun findSecondPrimaryConfigs(input: String): Set<SecondPrimaryConfig> {
        return find(secondPrimaryConfigs, input)
    }

    fun findPeriodBetweenUnitConfigs(input: String): Set<PeriodBetweenUnitConfig> {
        return find(periodBetweenUnitConfigs, input)
    }

    fun findComplicationConfigs(input: String): Set<ComplicationConfig> {
        return find(complicationConfigs, input)
    }

    fun findToxicityConfigs(input: String): Set<ToxicityConfig> {
        return find(toxicityConfigs, input)
    }

    fun findMolecularTestConfigs(input: String): Set<MolecularTestConfig> {
        return find(molecularTestConfigs, input)
    }

    fun findMedicationNameConfigs(input: String): Set<MedicationNameConfig> {
        return find(medicationNameConfigs, input)
    }

    fun findMedicationDosageConfigs(input: String): Set<MedicationDosageConfig> {
        return find(medicationDosageConfigs, input)
    }

    fun findIntoleranceConfigs(input: String): Set<IntoleranceConfig> {
        return find(intoleranceConfigs, input)
    }

    fun findCypInteractionConfigs(input: String): Set<CypInteractionConfig> {
        return find(cypInteractionConfigs, input)
    }

    fun findQTProlongingConfigs(input: String): Set<QTProlongatingConfig> {
        return find(qtProlongingConfigs, input)
    }

    fun translateAdministrationRoute(input: String): Translation? {
        return findTranslation(administrationRouteTranslations, input)
    }

    fun translateBloodTransfusion(input: String): Translation? {
        return findTranslation(bloodTransfusionTranslations, input)
    }

    fun translateDosageUnit(input: String): Translation? {
        return findTranslation(dosageUnitTranslations, input.lowercase())
    }

    fun translateToxicity(input: String): Translation? {
        return findTranslation(toxicityTranslations, input)
    }

    fun translateLabValue(code: String, name: String): LaboratoryTranslation? {
        return laboratoryTranslations[code to name]
    }

    fun evaluate(evaluatedInputs: ExtractionEvaluation, logger: Logger) {
        listOf(
            Triple(primaryTumorConfigs, evaluatedInputs.primaryTumorEvaluatedInputs, CurationCategory.PRIMARY_TUMOR),
            Triple(
                treatmentHistoryEntryConfigs, evaluatedInputs.treatmentHistoryEntryEvaluatedInputs, CurationCategory.ONCOLOGICAL_HISTORY
            ),
            Triple(secondPrimaryConfigs, evaluatedInputs.secondPrimaryEvaluatedInputs, CurationCategory.SECOND_PRIMARY),
            Triple(lesionLocationConfigs, evaluatedInputs.lesionLocationEvaluatedInputs, CurationCategory.LESION_LOCATION),
            Triple(
                nonOncologicalHistoryConfigs, evaluatedInputs.nonOncologicalHistoryEvaluatedInputs, CurationCategory.NON_ONCOLOGICAL_HISTORY
            ),
            Triple(ecgConfigs, evaluatedInputs.ecgEvaluatedInputs, CurationCategory.ECG),
            Triple(infectionConfigs, evaluatedInputs.infectionEvaluatedInputs, CurationCategory.INFECTION),
            Triple(
                periodBetweenUnitConfigs,
                evaluatedInputs.periodBetweenUnitEvaluatedInputs,
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION
            ),
            Triple(complicationConfigs, evaluatedInputs.complicationEvaluatedInputs, CurationCategory.COMPLICATION),
            Triple(toxicityConfigs, evaluatedInputs.toxicityEvaluatedInputs, CurationCategory.TOXICITY),
            Triple(molecularTestConfigs, evaluatedInputs.molecularTestEvaluatedInputs, CurationCategory.MOLECULAR_TEST),
            Triple(medicationNameConfigs, evaluatedInputs.medicationNameEvaluatedInputs, CurationCategory.MEDICATION_NAME),
            Triple(medicationDosageConfigs, evaluatedInputs.medicationDosageEvaluatedInputs, CurationCategory.MEDICATION_DOSAGE),
            Triple(intoleranceConfigs, evaluatedInputs.intoleranceEvaluatedInputs, CurationCategory.INTOLERANCE),
        ).forEach { (configMap, evaluatedInputs, category) ->
            (configMap.keys - evaluatedInputs).forEach { input ->
                logger.warn(" Curation key '{}' not used for {} curation", input, category.categoryName)
            }
        }

        listOf(
            Triple(
                administrationRouteTranslations,
                evaluatedInputs.administrationRouteEvaluatedInputs,
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ),
            Triple(toxicityTranslations, evaluatedInputs.toxicityTranslationEvaluatedInputs, CurationCategory.TOXICITY_TRANSLATION),
            Triple(dosageUnitTranslations, evaluatedInputs.dosageUnitEvaluatedInputs, CurationCategory.DOSAGE_UNIT_TRANSLATION)
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

    private fun <T : CurationConfig> find(configs: Map<String, Set<T>>, input: String): Set<T> {
        return configs[input.lowercase()] ?: emptySet()
    }

    private fun findTranslation(translations: Map<String, Translation>, input: String): Translation? {
        return translations[input.trim { it <= ' ' }]
    }

    companion object {
        fun <T : CurationConfig> asInputMap(configs: List<T>) =
            configs.groupBy { it.input.lowercase() }.mapValues { it.value.toSet() }
    }
}