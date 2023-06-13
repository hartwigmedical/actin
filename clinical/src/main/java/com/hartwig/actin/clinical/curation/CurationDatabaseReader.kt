package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.ComplicationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfigFile
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.ECGConfigFactory
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfigFactory
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfigFactory
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfigFactory
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfigFactory
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFactory
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfigFactory
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFactory
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfigFactory
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslationFactory
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslationFactory
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslationFactory
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslationFactory
import com.hartwig.actin.clinical.curation.translation.TranslationFile
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import java.io.IOException

class CurationDatabaseReader internal constructor(private val curationValidator: CurationValidator) {
    @Throws(IOException::class)
    fun read(clinicalCurationDirectory: String): CurationDatabase {
        LOGGER.info("Reading clinical curation config from {}", clinicalCurationDirectory)
        val basePath = Paths.forceTrailingFileSeparator(clinicalCurationDirectory)
        return ImmutableCurationDatabase.builder()
            .primaryTumorConfigs(readPrimaryTumorConfigs(basePath + PRIMARY_TUMOR_TSV))
            .oncologicalHistoryConfigs(readOncologicalHistoryConfigs(basePath + ONCOLOGICAL_HISTORY_TSV))
            .treatmentHistoryEntryConfigs(readTreatmentHistoryEntryConfigs(basePath + ONCOLOGICAL_HISTORY_TSV))
            .secondPrimaryConfigs(readSecondPrimaryConfigs(basePath + SECOND_PRIMARY_TSV))
            .lesionLocationConfigs(readLesionLocationConfigs(basePath + LESION_LOCATION_TSV))
            .nonOncologicalHistoryConfigs(readNonOncologicalHistoryConfigs(basePath + NON_ONCOLOGICAL_HISTORY_TSV))
            .ecgConfigs(readECGConfigs(basePath + ECG_TSV))
            .infectionConfigs(readInfectionConfigs(basePath + INFECTION_TSV))
            .complicationConfigs(readComplicationConfigs(basePath + COMPLICATION_TSV))
            .toxicityConfigs(readToxicityConfigs(basePath + TOXICITY_TSV))
            .molecularTestConfigs(readMolecularTestConfigs(basePath + MOLECULAR_TEST_TSV))
            .medicationNameConfigs(readMedicationNameConfigs(basePath + MEDICATION_NAME_TSV))
            .medicationDosageConfigs(readMedicationDosageConfigs(basePath + MEDICATION_DOSAGE_TSV))
            .medicationCategoryConfigs(readMedicationCategoryConfigs(basePath + MEDICATION_CATEGORY_TSV))
            .intoleranceConfigs(readIntoleranceConfigs(basePath + INTOLERANCE_TSV))
            .administrationRouteTranslations(readAdministrationRouteTranslations(basePath + ADMINISTRATION_ROUTE_TRANSLATION_TSV))
            .laboratoryTranslations(readLaboratoryTranslations(basePath + LABORATORY_TRANSLATION_TSV))
            .toxicityTranslations(readToxicityTranslations(basePath + TOXICITY_TRANSLATION_TSV))
            .bloodTransfusionTranslations(readBloodTransfusionTranslations(basePath + BLOOD_TRANSFUSION_TRANSLATION_TSV))
            .build()
    }

    @Throws(IOException::class)
    private fun readPrimaryTumorConfigs(tsv: String): List<PrimaryTumorConfig?> {
        val configs = CurationConfigFile.read(
            tsv, PrimaryTumorConfigFactory(
                curationValidator
            )
        )
        LOGGER.info(" Read {} primary tumor configs from {}", configs.size, tsv)
        return configs
    }

    @Throws(IOException::class)
    private fun readSecondPrimaryConfigs(tsv: String): List<SecondPrimaryConfig?> {
        val configs = CurationConfigFile.read(
            tsv, SecondPrimaryConfigFactory(
                curationValidator
            )
        )
        LOGGER.info(" Read {} second primary configs from {}", configs.size, tsv)
        return configs
    }

    @Throws(IOException::class)
    private fun readNonOncologicalHistoryConfigs(tsv: String): List<NonOncologicalHistoryConfig?> {
        val configs = CurationConfigFile.read(
            tsv, NonOncologicalHistoryConfigFactory(
                curationValidator
            )
        )
        LOGGER.info(" Read {} non-oncological history configs from {}", configs.size, tsv)
        return configs
    }

    @Throws(IOException::class)
    private fun readIntoleranceConfigs(tsv: String): List<IntoleranceConfig?> {
        val configs = CurationConfigFile.read(
            tsv, IntoleranceConfigFactory(
                curationValidator
            )
        )
        LOGGER.info(" Read {} intolerance configs from {}", configs.size, tsv)
        return configs
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
        private const val COMPLICATION_TSV = "complication.tsv"
        private const val TOXICITY_TSV = "toxicity.tsv"
        private const val MOLECULAR_TEST_TSV = "molecular_test.tsv"
        private const val MEDICATION_NAME_TSV = "medication_name.tsv"
        private const val MEDICATION_DOSAGE_TSV = "medication_dosage.tsv"
        private const val MEDICATION_CATEGORY_TSV = "medication_category.tsv"
        private const val INTOLERANCE_TSV = "intolerance.tsv"
        private const val ADMINISTRATION_ROUTE_TRANSLATION_TSV = "administration_route_translation.tsv"
        private const val LABORATORY_TRANSLATION_TSV = "laboratory_translation.tsv"
        private const val TOXICITY_TRANSLATION_TSV = "toxicity_translation.tsv"
        private const val BLOOD_TRANSFUSION_TRANSLATION_TSV = "blood_transfusion_translation.tsv"

        @Throws(IOException::class)
        private fun readOncologicalHistoryConfigs(tsv: String): List<OncologicalHistoryConfig?> {
            val configs = CurationConfigFile.read(tsv, OncologicalHistoryConfigFactory())
            LOGGER.info(" Read {} oncological history configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readTreatmentHistoryEntryConfigs(tsv: String): List<TreatmentHistoryEntryConfig?> {
            val configs = CurationConfigFile.read(tsv, TreatmentHistoryEntryConfigFactory())
            LOGGER.info(" Read {} treatment history entry configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readLesionLocationConfigs(tsv: String): List<LesionLocationConfig?> {
            val configs = CurationConfigFile.read(tsv, LesionLocationConfigFactory())
            LOGGER.info(" Read {} lesion location configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readECGConfigs(tsv: String): List<ECGConfig?> {
            val configs = CurationConfigFile.read(tsv, ECGConfigFactory())
            LOGGER.info(" Read {} ECG configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readInfectionConfigs(tsv: String): List<InfectionConfig?> {
            val configs = CurationConfigFile.read(tsv, InfectionConfigFactory())
            LOGGER.info(" Read {} infection configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readComplicationConfigs(tsv: String): List<ComplicationConfig?> {
            val configs = CurationConfigFile.read(tsv, ComplicationConfigFactory())
            LOGGER.info(" Read {} complication configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readToxicityConfigs(tsv: String): List<ToxicityConfig?> {
            val configs = CurationConfigFile.read(tsv, ToxicityConfigFactory())
            LOGGER.info(" Read {} toxicity configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readMolecularTestConfigs(tsv: String): List<MolecularTestConfig?> {
            val configs = CurationConfigFile.read(tsv, MolecularTestConfigFactory())
            LOGGER.info(" Read {} molecular test configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readMedicationNameConfigs(tsv: String): List<MedicationNameConfig?> {
            val configs = CurationConfigFile.read(tsv, MedicationNameConfigFactory())
            LOGGER.info(" Read {} medication name configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readMedicationDosageConfigs(tsv: String): List<MedicationDosageConfig?> {
            val configs = CurationConfigFile.read(tsv, MedicationDosageConfigFactory())
            LOGGER.info(" Read {} medication dosage configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readMedicationCategoryConfigs(tsv: String): List<MedicationCategoryConfig?> {
            val configs = CurationConfigFile.read(tsv, MedicationCategoryConfigFactory())
            LOGGER.info(" Read {} medication category configs from {}", configs.size, tsv)
            return configs
        }

        @Throws(IOException::class)
        private fun readAdministrationRouteTranslations(tsv: String): List<AdministrationRouteTranslation?> {
            val translations = TranslationFile.read(tsv, AdministrationRouteTranslationFactory())
            LOGGER.info(" Read {} administration route translations from {}", translations.size, tsv)
            return translations
        }

        @Throws(IOException::class)
        private fun readLaboratoryTranslations(tsv: String): List<LaboratoryTranslation?> {
            val translations = TranslationFile.read(tsv, LaboratoryTranslationFactory())
            LOGGER.info(" Read {} laboratory translations from {}", translations.size, tsv)
            return translations
        }

        @Throws(IOException::class)
        private fun readToxicityTranslations(tsv: String): List<ToxicityTranslation?> {
            val translations = TranslationFile.read(tsv, ToxicityTranslationFactory())
            LOGGER.info(" Read {} toxicity translations from {}", translations.size, tsv)
            return translations
        }

        @Throws(IOException::class)
        private fun readBloodTransfusionTranslations(tsv: String): List<BloodTransfusionTranslation?> {
            val translations = TranslationFile.read(tsv, BloodTransfusionTranslationFactory())
            LOGGER.info(" Read {} blood transfusion translations from {}", translations.size, tsv)
            return translations
        }
    }
}