package com.hartwig.actin.trial

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class TrialCreatorConfig(
    val ctcConfigDirectory: String?,
    val trialConfigDirectory: String,
    val treatmentDirectory: String,
    val doidJson: String,
    val knownGenesTsv: String,
    val outputDirectory: String
) {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(TrialCreatorConfig::class.java)

        private const val CTC_CONFIG_DIRECTORY = "ctc_config_directory"
        private const val TRIAL_CONFIG_DIRECTORY = "trial_config_directory"
        private const val TREATMENT_DIRECTORY = "treatment_directory"
        private const val DOID_JSON = "doid_json"
        private const val KNOWN_GENES_TSV = "known_genes_tsv"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val LOG_DEBUG = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(CTC_CONFIG_DIRECTORY, true, "Directory containing the CTC (clinical trial center) config files")
            options.addOption(TRIAL_CONFIG_DIRECTORY, true, "Directory containing the trial config files")
            options.addOption(TREATMENT_DIRECTORY, true, "Directory containing the treatment database")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(KNOWN_GENES_TSV, true, "A TSV containing genes which are allowed as valid genes in trial config")
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
                ctcConfigDirectory = ApplicationConfig.optionalDir(cmd, CTC_CONFIG_DIRECTORY),
                trialConfigDirectory = ApplicationConfig.nonOptionalDir(cmd, TRIAL_CONFIG_DIRECTORY),
                treatmentDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                knownGenesTsv = ApplicationConfig.nonOptionalFile(cmd, KNOWN_GENES_TSV),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
            )
        }
    }
}