package com.hartwig.actin.algo

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class TreatmentMatcherConfig(
    val clinicalJson: String,
    val molecularJson: String,
    val treatmentDatabaseDirectory: String,
    val doidJson: String,
    val outputDirectory: String,
    val runHistorically: Boolean,
) {

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the patient")
            options.addOption(MOLECULAR_JSON, true, "File containing the most recent molecular record of the patient")
            options.addOption(TREATMENT_DATABASE_DIRECTORY, true, "Directory containing all available treatments")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where the matcher output will be written to")
            options.addOption(
                RUN_HISTORICALLY,
                false,
                "If set, runs the algo with a date just after the original patient registration date"
            )
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): TreatmentMatcherConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            val runHistorically = cmd.hasOption(RUN_HISTORICALLY)
            if (runHistorically) {
                LOGGER.info("Configured to run in historic mode")
            }
            return TreatmentMatcherConfig(
                clinicalJson = ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON),
                molecularJson = ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON),
                treatmentDatabaseDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DATABASE_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
                runHistorically = runHistorically,
            )
        }

        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherConfig::class.java)
        private const val CLINICAL_JSON = "clinical_json"
        private const val MOLECULAR_JSON = "molecular_json"
        private const val TREATMENT_DATABASE_DIRECTORY = "treatment_database_directory"
        private const val DOID_JSON = "doid_json"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val RUN_HISTORICALLY = "run_historically"
        private const val LOG_DEBUG = "log_debug"
    }
}