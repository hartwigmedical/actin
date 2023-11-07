package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
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
import org.apache.logging.log4j.LogManager

private val LOGGER = LogManager.getLogger(CurationDatabase::class.java)
typealias InputText = String

data class CurationDatabase(
    val primaryTumorConfigs: Map<InputText, Set<PrimaryTumorConfig>>,
    val treatmentHistoryEntryConfigs: Map<InputText, Set<TreatmentHistoryEntryConfig>>,
    val secondPrimaryConfigs: Map<InputText, Set<SecondPrimaryConfig>>,
    val lesionLocationConfigs: Map<InputText, Set<LesionLocationConfig>>,
    val nonOncologicalHistoryConfigs: Map<InputText, Set<NonOncologicalHistoryConfig>>,
    val ecgConfigs: Map<InputText, Set<ECGConfig>>,
    val infectionConfigs: Map<InputText, Set<InfectionConfig>>,
    val periodBetweenUnitConfigs: Map<InputText, Set<PeriodBetweenUnitConfig>>,
    val complicationConfigs: Map<InputText, Set<ComplicationConfig>>,
    val toxicityConfigs: Map<InputText, Set<ToxicityConfig>>,
    val molecularTestConfigs: Map<InputText, Set<MolecularTestConfig>>,
    val medicationNameConfigs: Map<InputText, Set<MedicationNameConfig>>,
    val medicationDosageConfigs: Map<InputText, Set<MedicationDosageConfig>>,
    val intoleranceConfigs: Map<InputText, Set<IntoleranceConfig>>,
    val cypInteractionConfigs: Map<InputText, Set<CypInteractionConfig>>,
    val qtProlongingConfigs: Map<InputText, Set<QTProlongatingConfig>>,
    val administrationRouteTranslations: Map<InputText, Translation>,
    val laboratoryTranslations: Map<Pair<InputText, InputText>, LaboratoryTranslation>,
    val toxicityTranslations: Map<InputText, Translation>,
    val bloodTransfusionTranslations: Map<InputText, Translation>,
    val dosageUnitTranslations: Map<InputText, Translation>
) {

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
   
    fun translateAdministrationRoute(input: String): String? {
        return findTranslation(administrationRouteTranslations, input)
    }

    fun translateBloodTransfusion(input: String): String? {
        return findTranslation(bloodTransfusionTranslations, input)
    }

    fun translateDosageUnit(input: String): String? {
        return findTranslation(dosageUnitTranslations, input.lowercase())
    }

    fun translateToxicity(input: String): String? {
        return findTranslation(toxicityTranslations, input)
    }

    fun translateLabValue(code: String, name: String): LaboratoryTranslation? {
        return laboratoryTranslations[code to name]
    }

    fun evaluate(evaluatedInputs: ExtractionEvaluation) {
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
            Triple(
                administrationRouteTranslations,
                evaluatedInputs.administrationRouteEvaluatedInputs,
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ),
            Triple(
                laboratoryTranslations.mapKeys { (key, _) -> key.toList().joinToString("|") },
                evaluatedInputs.laboratoryEvaluatedInputs,
                CurationCategory.LABORATORY_TRANSLATION
            ),
            Triple(toxicityTranslations, evaluatedInputs.toxicityEvaluatedInputs, CurationCategory.TOXICITY_TRANSLATION),
            Triple(dosageUnitTranslations, evaluatedInputs.dosageUnitEvaluatedInputs, CurationCategory.DOSAGE_UNIT_TRANSLATION)
        ).forEach { (configMap, evaluatedInputs, category) ->
            (configMap.keys - evaluatedInputs).forEach { input ->
                LOGGER.warn(" Curation key '{}' not used for {} curation", input, category.categoryName)
            }
        }
    }

    private fun <T : CurationConfig> find(configs: Map<String, Set<T>>, input: String): Set<T> {
        return configs[input.lowercase()] ?: emptySet()
    }

    private fun findTranslation(translations: Map<String, Translation>, input: String): String? {
        return translations[input.trim { it <= ' ' }]?.translated
    }
}