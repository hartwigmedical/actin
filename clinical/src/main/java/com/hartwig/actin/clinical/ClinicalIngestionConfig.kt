package com.hartwig.actin.clinical

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
interface ClinicalIngestionConfig {
    fun feedDirectory(): String
    fun curationDirectory(): String
    fun doidJson(): String
    fun outputDirectory(): String

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(FEED_DIRECTORY, true, "Directory containing the clinical feed data")
            options.addOption(CURATION_DIRECTORY, true, "Directory containing the clinical curation config data")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): ClinicalIngestionConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return ImmutableClinicalIngestionConfig.builder()
                .feedDirectory(ApplicationConfig.nonOptionalDir(cmd, FEED_DIRECTORY))
                .curationDirectory(ApplicationConfig.nonOptionalDir(cmd, CURATION_DIRECTORY))
                .doidJson(ApplicationConfig.nonOptionalFile(cmd, DOID_JSON))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build()
        }

        val LOGGER = LogManager.getLogger(ClinicalIngestionConfig::class.java)
        const val FEED_DIRECTORY = "feed_directory"
        const val CURATION_DIRECTORY = "curation_directory"
        const val DOID_JSON = "doid_json"
        const val OUTPUT_DIRECTORY = "output_directory"
        const val LOG_DEBUG = "log_debug"
    }
}