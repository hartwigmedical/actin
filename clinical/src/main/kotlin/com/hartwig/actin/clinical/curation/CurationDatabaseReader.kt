package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfigFile
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
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
        const val MOLECULAR_TEST_IHC_TSV = "molecular_test_ihc.tsv"
        const val MOLECULAR_TEST_PDL1_TSV = "molecular_test_pdl1.tsv"
        const val SEQUENCING_TEST_TSV = "sequencing_test.tsv"
        const val MEDICATION_NAME_TSV = "medication_name.tsv"
        const val MEDICATION_DOSAGE_TSV = "medication_dosage.tsv"
        const val INTOLERANCE_TSV = "intolerance.tsv"
        const val SURGERY_NAME_TSV = "surgery_name.tsv"
        const val LABORATORY_TSV = "laboratory.tsv"

        fun <T : CurationConfig> read(
            clinicalCurationDirectory: String,
            tsv: String,
            factory: CurationConfigFactory<T>,
            category: CurationCategory,
            evaluatedInputFunction: (CurationExtractionEvaluation) -> Set<String>
        ): CurationDatabase<T> {
            LOGGER.info("Reading clinical curation config from {}", clinicalCurationDirectory)
            val basePath = Paths.forceTrailingFileSeparator(clinicalCurationDirectory)
            val filePath = basePath + tsv
            val configs = CurationConfigFile.read(filePath, factory)
            LOGGER.info(" Read {} configs from {}", configs.size, filePath)
            return CurationDatabase.create(category, evaluatedInputFunction, configs)
        }
    }
}

