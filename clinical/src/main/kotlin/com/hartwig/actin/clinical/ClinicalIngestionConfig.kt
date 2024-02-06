package com.hartwig.actin.clinical

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

enum class FeedFormat {
    DEFAULT_EXTERNAL_JSON,
    EMC_TSV
}

data class ClinicalIngestionConfig(
    val feedDirectory: String,
    val curationDirectory: String,
    val doidJson: String,
    val atcTsv: String,
    val treatmentDirectory: String,
    val outputDirectory: String,
    val feedFormat: FeedFormat
) {

    companion object {
        val LOGGER: Logger = LogManager.getLogger(ClinicalIngestionConfig::class.java)
        private const val FEED_DIRECTORY = "feed_directory"
        private const val CURATION_DIRECTORY = "curation_directory"
        private const val DOID_JSON = "doid_json"
        private const val ATC_TSV = "atc_tsv"
        private const val TREATMENT_DIRECTORY = "treatment_directory"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val LOG_DEBUG = "log_debug"
        private const val FEED_FORMAT = "feed_format"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(FEED_DIRECTORY, true, "Directory containing the clinical feed data")
            options.addOption(CURATION_DIRECTORY, true, "Directory containing the clinical curation config data")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(ATC_TSV, true, "Path to TSV file container the full ATC tree")
            options.addOption(TREATMENT_DIRECTORY, true, "Directory containing the treatment data")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            options.addOption(FEED_FORMAT, true, "The [${FeedFormat.values().joinToString()}]")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): ClinicalIngestionConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return ClinicalIngestionConfig(
                feedDirectory = ApplicationConfig.nonOptionalDir(cmd, FEED_DIRECTORY),
                curationDirectory = ApplicationConfig.nonOptionalDir(cmd, CURATION_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                atcTsv = ApplicationConfig.nonOptionalFile(cmd, ATC_TSV),
                treatmentDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DIRECTORY),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
                feedFormat = ApplicationConfig.nonOptionalValue(cmd, FEED_FORMAT).let { FeedFormat.valueOf(it) }
            )
        }
    }
}