package com.hartwig.actin.report

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class TabularTreatmentMatchWriterConfig(
    val treatmentMatchJson: String,
    val outputDirectory: String
) {

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TabularTreatmentMatchWriterConfig::class.java)

        private const val TREATMENT_MATCH_JSON = "treatment_match_json"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val LOG_DEBUG = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(TREATMENT_MATCH_JSON, true, "File containing all available treatments, matched to the patient")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): TabularTreatmentMatchWriterConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }

            return TabularTreatmentMatchWriterConfig(
                treatmentMatchJson = ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }
    }
}