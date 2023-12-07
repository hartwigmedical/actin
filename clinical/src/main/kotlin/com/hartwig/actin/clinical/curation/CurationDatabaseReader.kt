package com.hartwig.actin.clinical.curation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.ComplicationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfigFile
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfigFactory
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.ECGConfigFactory
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfigFactory
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfigFactory
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfigFactory
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfigFactory
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfigFactory
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFactory
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfigFactory
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfigFactory
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFactory
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfigFactory
import com.hartwig.actin.clinical.curation.config.ValidatedCurationConfig
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class CurationDatabaseReader {

    companion object {
        val LOGGER: Logger = LogManager.getLogger(CurationDatabaseReader::class.java)

        const val PRIMARY_TUMOR_TSV = "primary_tumor.tsv"
        const val ONCOLOGICAL_HISTORY_TSV = "oncological_history.tsv"
        const val SECOND_PRIMARY_TSV = "second_primary.tsv"
        const val LESION_LOCATION_TSV = "lesion_location.tsv"
        const val NON_ONCOLOGICAL_HISTORY_TSV = "non_oncological_history.tsv"
        const val ECG_TSV = "ecg.tsv"
        const val INFECTION_TSV = "infection.tsv"
        const val PERIOD_BETWEEN_UNIT_TSV = "period_between_unit_interpretation.tsv"
        const val COMPLICATION_TSV = "complication.tsv"
        const val TOXICITY_TSV = "toxicity.tsv"
        const val MOLECULAR_TEST_TSV = "molecular_test.tsv"
        const val MEDICATION_NAME_TSV = "medication_name.tsv"
        const val MEDICATION_DOSAGE_TSV = "medication_dosage.tsv"
        const val INTOLERANCE_TSV = "intolerance.tsv"
        const val CYP_INTERACTIONS_TSV = "cyp_interactions.tsv"
        const val QT_PROLONGATING_TSV = "qt_prolongating.tsv"

        fun <T : CurationConfig> read(
            clinicalCurationDirectory: String,
            tsv: String,
            factory: CurationConfigFactory<T>
        ): CurationDatabase<T> {
            LOGGER.info("Reading clinical curation config from {}", clinicalCurationDirectory)
            val basePath = Paths.forceTrailingFileSeparator(clinicalCurationDirectory)
            return readConfigs(basePath, tsv, factory)
        }

        private fun <T : CurationConfig> readConfigs(
            basePath: String,
            tsv: String,
            configFactory: CurationConfigFactory<T>
        ): CurationDatabase<T> {
            val filePath = basePath + tsv
            val configs = CurationConfigFile.read(filePath, configFactory)
            LOGGER.info(" Read {} configs from {}", configs.size, filePath)
            return CurationDatabase(asInputMap(configs.map { it }))
        }

        private fun <T : CurationConfig> asInputMap(configs: List<ValidatedCurationConfig<T>>) =
            configs.groupBy { it.config.input.lowercase() }.mapValues { it.value.toSet() }


    }

}

