package com.hartwig.actin.clinical

import com.hartwig.actin.configuration.OVERRIDE_YAML_ARGUMENT
import com.hartwig.actin.configuration.OVERRIDE_YAML_DESCRIPTION
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

enum class FeedFormat {
    STANDARD_JSON,
    EMC_TSV
}

data class ClinicalIngestionConfig(
    val feedDirectory: String,
    val curationDirectory: String,
    val doidJson: String,
    val icdTsv: String,
    val drugInteractionsTsv: String,
    val qtProlongatingTsv: String,
    val atcTsv: String,
    val atcOverridesTsv: String,
    val treatmentDirectory: String,
    val outputDirectory: String,
    val feedFormat: FeedFormat,
    val overridesYaml: String?
) {

    companion object {
        val LOGGER: Logger = LogManager.getLogger(ClinicalIngestionConfig::class.java)

        private const val FEED_DIRECTORY = "feed_directory"
        private const val CURATION_DIRECTORY = "curation_directory"
        private const val DOID_JSON = "doid_json"
        private const val ICD_TSV = "icd_tsv"
        private const val DRUG_INTERACTIONS_TSV = "drug_interactions_tsv"
        private const val QT_PROLONGATING_TSV = "qt_prolongating_tsv"
        private const val ATC_TSV = "atc_tsv"
        private const val ATC_OVERRIDES_TSV = "atc_overrides_tsv"
        private const val TREATMENT_DIRECTORY = "treatment_directory"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val LOG_DEBUG = "log_debug"
        private const val FEED_FORMAT = "feed_format"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(FEED_DIRECTORY, true, "Directory containing the clinical feed data")
            options.addOption(CURATION_DIRECTORY, true, "Directory containing the clinical curation config data")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(ICD_TSV, true, "Path to TSV file containing the full ICD-11 tree")
            options.addOption(DRUG_INTERACTIONS_TSV, true, "Path to TSV file containing drug interactions")
            options.addOption(QT_PROLONGATING_TSV, true, "Path to TSV file containing QT prolongating drugs")
            options.addOption(ATC_TSV, true, "Path to TSV file containing the full ATC tree")
            options.addOption(ATC_OVERRIDES_TSV, true, "Path to TSV file containing ATC code overrides")
            options.addOption(TREATMENT_DIRECTORY, true, "Directory containing the treatment data")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            options.addOption(
                FEED_FORMAT,
                true,
                "The format of the feed. Accepted values [${FeedFormat.entries.joinToString()}]. Default is ${FeedFormat.EMC_TSV.name}."
            )
            options.addOption(OVERRIDE_YAML_ARGUMENT, true, OVERRIDE_YAML_DESCRIPTION)
            return options
        }

        fun createConfig(cmd: CommandLine): ClinicalIngestionConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return ClinicalIngestionConfig(
                feedDirectory = ApplicationConfig.nonOptionalDir(cmd, FEED_DIRECTORY),
                curationDirectory = ApplicationConfig.nonOptionalDir(cmd, CURATION_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                icdTsv = ApplicationConfig.nonOptionalFile(cmd, ICD_TSV),
                drugInteractionsTsv = ApplicationConfig.nonOptionalFile(cmd, DRUG_INTERACTIONS_TSV),
                qtProlongatingTsv = ApplicationConfig.nonOptionalFile(cmd, QT_PROLONGATING_TSV),
                atcTsv = ApplicationConfig.nonOptionalFile(cmd, ATC_TSV),
                atcOverridesTsv = ApplicationConfig.nonOptionalFile(cmd, ATC_OVERRIDES_TSV),
                treatmentDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DIRECTORY),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
                feedFormat = ApplicationConfig.optionalValue(cmd, FEED_FORMAT)?.let { FeedFormat.valueOf(it) } ?: FeedFormat.EMC_TSV,
                overridesYaml = ApplicationConfig.optionalFile(cmd, OVERRIDE_YAML_ARGUMENT)
            )
        }
    }
}