package com.hartwig.actin.report

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
interface TabularTreatmentMatchWriterConfig {
    fun treatmentMatchJson(): String
    fun outputDirectory(): String

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(TREATMENT_MATCH_JSON, true, "File containing all available treatments, matched to the patient")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): TabularTreatmentMatchWriterConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return ImmutableTabularTreatmentMatchWriterConfig.builder()
                .treatmentMatchJson(ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build()
        }

        val LOGGER = LogManager.getLogger(TabularTreatmentMatchWriterConfig::class.java)
        const val TREATMENT_MATCH_JSON = "treatment_match_json"
        const val OUTPUT_DIRECTORY = "output_directory"
        const val LOG_DEBUG = "log_debug"
    }
}