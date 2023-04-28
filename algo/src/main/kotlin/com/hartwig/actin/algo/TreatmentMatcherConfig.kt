package com.hartwig.actin.algo

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
interface TreatmentMatcherConfig {
    open fun clinicalJson(): String
    open fun molecularJson(): String
    open fun treatmentDatabaseDirectory(): String
    open fun doidJson(): String
    open fun outputDirectory(): String
    open fun runHistorically(): Boolean

    companion object {
        open fun createOptions(): Options {
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
        open fun createConfig(cmd: CommandLine): TreatmentMatcherConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            val runHistorically = cmd.hasOption(RUN_HISTORICALLY)
            if (runHistorically) {
                LOGGER.info("Configured to run in historic mode")
            }
            return ImmutableTreatmentMatcherConfig.builder()
                .clinicalJson(ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON))
                .molecularJson(ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON))
                .treatmentDatabaseDirectory(ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DATABASE_DIRECTORY))
                .doidJson(ApplicationConfig.nonOptionalFile(cmd, DOID_JSON))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .runHistorically(runHistorically)
                .build()
        }

        val LOGGER = LogManager.getLogger(TreatmentMatcherConfig::class.java)
        val CLINICAL_JSON: String? = "clinical_json"
        val MOLECULAR_JSON: String? = "molecular_json"
        val TREATMENT_DATABASE_DIRECTORY: String? = "treatment_database_directory"
        val DOID_JSON: String? = "doid_json"
        val OUTPUT_DIRECTORY: String? = "output_directory"
        val RUN_HISTORICALLY: String? = "run_historically"
        val LOG_DEBUG: String? = "log_debug"
    }
}