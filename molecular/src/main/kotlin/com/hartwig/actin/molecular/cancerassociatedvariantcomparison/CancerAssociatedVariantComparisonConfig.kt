package com.hartwig.actin.molecular.cancerassociatedvariantcomparison

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

data class CancerAssociatedVariantComparisonConfig(
    val orangeJson: String,
    val serveDirectory: String,
    val outputDirectory: String
) {

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val ORANGE_JSON: String = "orange_json"
        private const val SERVE_DIRECTORY: String = "serve_directory"
        private const val OUTPUT_DIRECTORY: String = "output_directory"
        private const val LOG_DEBUG: String = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(ORANGE_JSON, true, "Path of the ORANGE json to be interpreted")
            options.addOption(SERVE_DIRECTORY, true, "Path towards the SERVE directory containing known events")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where cancer-associated variant comparison output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): CancerAssociatedVariantComparisonConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).level = Level.DEBUG
                logger.debug { "Switched root level logging to DEBUG" }
            }
            return CancerAssociatedVariantComparisonConfig(
                orangeJson = ApplicationConfig.nonOptionalFile(cmd, ORANGE_JSON),
                serveDirectory = ApplicationConfig.nonOptionalDir(cmd, SERVE_DIRECTORY),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }
    }
}
