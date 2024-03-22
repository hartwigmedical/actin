package com.hartwig.actin.report

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator

data class ReporterConfig(
    val clinicalJson: String,
    val molecularJson: String?,
    val treatmentMatchJson: String,
    val outputDirectory: String,
    val enableExtendedMode: Boolean
) {

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the patient")
            options.addOption(MOLECULAR_JSON, true, "File containing the most recent molecular record of the patient")
            options.addOption(TREATMENT_MATCH_JSON, true, "File containing all available treatments, matched to the patient")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where the report will be written to")
            options.addOption(ENABLE_EXTENDED_MODE, false, "If set, includes trial matching details")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): ReporterConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            val enableExtendedMode = cmd.hasOption(ENABLE_EXTENDED_MODE)
            if (enableExtendedMode) {
                LOGGER.info("Extended reporting mode has been enabled")
            }
            return ReporterConfig(
                clinicalJson = ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON),
                molecularJson = ApplicationConfig.optionalFile(cmd, MOLECULAR_JSON),
                treatmentMatchJson = ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
                enableExtendedMode = enableExtendedMode
            )
        }

        val LOGGER = LogManager.getLogger(ReporterConfig::class.java)
        const val CLINICAL_JSON = "clinical_json"
        const val MOLECULAR_JSON = "molecular_json"
        const val TREATMENT_MATCH_JSON = "treatment_match_json"
        const val OUTPUT_DIRECTORY = "output_directory"
        const val ENABLE_EXTENDED_MODE = "enable_extended_mode"
        const val LOG_DEBUG = "log_debug"
    }
}