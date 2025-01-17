package com.hartwig.actin.trial

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class TrialCreatorConfig(
    val trialConfigJsonPath: String,
    val treatmentDirectory: String,
    val doidJson: String,
    val icdTsv: String,
    val atcTsv: String,
    val serveDbJson: String,
    val outputDirectory: String
) {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(TrialCreatorConfig::class.java)

        private const val TRIAL_CONFIG_JSON_PATH = "trial_config_json_path"
        private const val TREATMENT_DIRECTORY = "treatment_directory"
        private const val DOID_JSON = "doid_json"
        private const val ICD_TSV = "icd_tsv"
        private const val ATC_TSV = "atc_tsv"
        private const val SERVE_DB_JSON = "serve_db_json"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val LOG_DEBUG = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(TRIAL_CONFIG_JSON_PATH, true, "Path to the trial config JSON file")
            options.addOption(TREATMENT_DIRECTORY, true, "Directory containing the treatment database")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(ICD_TSV, true, "Path to TSV file containing the full ICD-11 tree")
            options.addOption(ATC_TSV, true, "Path to TSV file container the full ATC tree")
            options.addOption(SERVE_DB_JSON, true, "SERVE DB json file containing genes which are allowed as valid genes in trial config")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where treatment data will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): TrialCreatorConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }

            return TrialCreatorConfig(
                trialConfigJsonPath = ApplicationConfig.nonOptionalFile(cmd, TRIAL_CONFIG_JSON_PATH),
                treatmentDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                icdTsv = ApplicationConfig.nonOptionalFile(cmd, ICD_TSV),
                atcTsv = ApplicationConfig.nonOptionalFile(cmd, ATC_TSV),
                serveDbJson = ApplicationConfig.nonOptionalFile(cmd, SERVE_DB_JSON),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }
    }
}