package com.hartwig.actin.clinical.curation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.config.ComplicationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfigFile
import com.hartwig.actin.clinical.curation.config.CypInteractionConfigFactory
import com.hartwig.actin.clinical.curation.config.ECGConfigFactory
import com.hartwig.actin.clinical.curation.config.InfectionConfigFactory
import com.hartwig.actin.clinical.curation.config.IntoleranceConfigFactory
import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationNameConfigFactory
import com.hartwig.actin.clinical.curation.config.MolecularTestConfigFactory
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfigFactory
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFactory
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfigFactory
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfigFactory
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFactory
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryCurationConfigFile
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslationFactory
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslationFactory
import com.hartwig.actin.clinical.curation.translation.DosageUnitTranslationFactory
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslationFactory
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslationFactory
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationFactory
import com.hartwig.actin.clinical.curation.translation.TranslationFile
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import java.io.IOException

class CurationDatabaseReader(private val curationValidator: CurationValidator, private val treatmentDatabase: TreatmentDatabase) {
    @Throws(IOException::class)
    fun read(clinicalCurationDirectory: String): CurationDatabase {
        LOGGER.info("Reading clinical curation config from {}", clinicalCurationDirectory)
        val basePath = Paths.forceTrailingFileSeparator(clinicalCurationDirectory)

        return CurationDatabase(
            primaryTumorConfigs = readConfigs(basePath, PRIMARY_TUMOR_TSV, PrimaryTumorConfigFactory(curationValidator)),
            treatmentHistoryEntryConfigs = TreatmentHistoryCurationConfigFile.read(basePath + ONCOLOGICAL_HISTORY_TSV, treatmentDatabase),
            oncologicalHistoryConfigs = readConfigs(basePath, ONCOLOGICAL_HISTORY_TSV, OncologicalHistoryConfigFactory()),
            secondPrimaryConfigs = readConfigs(basePath, SECOND_PRIMARY_TSV, SecondPrimaryConfigFactory(curationValidator)),
            lesionLocationConfigs = readConfigs(basePath, LESION_LOCATION_TSV, LesionLocationConfigFactory()),
            nonOncologicalHistoryConfigs = readConfigs(
                basePath,
                NON_ONCOLOGICAL_HISTORY_TSV,
                NonOncologicalHistoryConfigFactory(curationValidator)
            ),
            ecgConfigs = readConfigs(basePath, ECG_TSV, ECGConfigFactory()),
            infectionConfigs = readConfigs(basePath, INFECTION_TSV, InfectionConfigFactory()),
            periodBetweenUnitConfigs = readConfigs(basePath, PERIOD_BETWEEN_UNIT_TSV, PeriodBetweenUnitConfigFactory()),
            complicationConfigs = readConfigs(basePath, COMPLICATION_TSV, ComplicationConfigFactory()),
            toxicityConfigs = readConfigs(basePath, TOXICITY_TSV, ToxicityConfigFactory()),
            molecularTestConfigs = readConfigs(basePath, MOLECULAR_TEST_TSV, MolecularTestConfigFactory()),
            medicationNameConfigs = readConfigs(basePath, MEDICATION_NAME_TSV, MedicationNameConfigFactory()),
            medicationDosageConfigs = readConfigs(basePath, MEDICATION_DOSAGE_TSV, MedicationDosageConfigFactory()),
            medicationCategoryConfigs = readConfigs(basePath, MEDICATION_CATEGORY_TSV, MedicationCategoryConfigFactory()),
            intoleranceConfigs = readConfigs(basePath, INTOLERANCE_TSV, IntoleranceConfigFactory(curationValidator)),
            cypInteractionConfigs = readConfigs(basePath, CYP_INTERACTIONS_TSV, CypInteractionConfigFactory()),
            qtProlongingConfigs = readConfigs(basePath, QT_PROLONGING_TSV, QTProlongatingConfigFactory()),
            administrationRouteTranslations = readTranslations(
                basePath,
                ADMINISTRATION_ROUTE_TRANSLATION_TSV,
                AdministrationRouteTranslationFactory()
            ),
            laboratoryTranslations = readTranslations(basePath, LABORATORY_TRANSLATION_TSV, LaboratoryTranslationFactory()),
            toxicityTranslations = readTranslations(basePath, TOXICITY_TRANSLATION_TSV, ToxicityTranslationFactory()),
            bloodTransfusionTranslations = readTranslations(
                basePath,
                BLOOD_TRANSFUSION_TRANSLATION_TSV,
                BloodTransfusionTranslationFactory()
            ),
            dosageUnitTranslations = readTranslations(basePath, DOSAGE_UNIT_TRANSLATION_TSV, DosageUnitTranslationFactory()),
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CurationDatabaseReader::class.java)

        private const val PRIMARY_TUMOR_TSV = "primary_tumor.tsv"
        private const val ONCOLOGICAL_HISTORY_TSV = "oncological_history.tsv"
        private const val SECOND_PRIMARY_TSV = "second_primary.tsv"
        private const val LESION_LOCATION_TSV = "lesion_location.tsv"
        private const val NON_ONCOLOGICAL_HISTORY_TSV = "non_oncological_history.tsv"
        private const val ECG_TSV = "ecg.tsv"
        private const val INFECTION_TSV = "infection.tsv"
        private const val PERIOD_BETWEEN_UNIT_TSV = "period_between_unit_interpretation.tsv"
        private const val COMPLICATION_TSV = "complication.tsv"
        private const val TOXICITY_TSV = "toxicity.tsv"
        private const val MOLECULAR_TEST_TSV = "molecular_test.tsv"
        private const val MEDICATION_NAME_TSV = "medication_name.tsv"
        private const val MEDICATION_DOSAGE_TSV = "medication_dosage.tsv"
        private const val MEDICATION_CATEGORY_TSV = "medication_category.tsv"
        private const val INTOLERANCE_TSV = "intolerance.tsv"
        private const val CYP_INTERACTIONS_TSV = "cyp_interactions.tsv"
        private const val QT_PROLONGING_TSV = "qt_prolonging.tsv"
        private const val ADMINISTRATION_ROUTE_TRANSLATION_TSV = "administration_route_translation.tsv"
        private const val LABORATORY_TRANSLATION_TSV = "laboratory_translation.tsv"
        private const val TOXICITY_TRANSLATION_TSV = "toxicity_translation.tsv"
        private const val BLOOD_TRANSFUSION_TRANSLATION_TSV = "blood_transfusion_translation.tsv"
        private const val DOSAGE_UNIT_TRANSLATION_TSV = "dosage_unit_translation.tsv"

        @Throws(IOException::class)
        private fun <T : CurationConfig> readConfigs(basePath: String, tsv: String, configFactory: CurationConfigFactory<T>): List<T> {
            val filePath = basePath + tsv
            val configs = CurationConfigFile.read(filePath, configFactory)
            LOGGER.info(" Read {} configs from {}", configs.size, filePath)
            return configs
        }

        @Throws(IOException::class)
        private fun <T : Translation> readTranslations(basePath: String, tsv: String, translationFactory: TranslationFactory<T>): List<T> {
            val filePath = basePath + tsv
            val translations = TranslationFile.read(filePath, translationFactory)
            LOGGER.info(" Read {} translations from {}", translations.size, filePath)
            return translations
        }
    }
}