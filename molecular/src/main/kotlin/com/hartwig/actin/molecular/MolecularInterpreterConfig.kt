package com.hartwig.actin.molecular

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class MolecularInterpreterConfig(
    val clinicalJson: String,
    val orangeJson: String?,
    val serveDirectory: String,
    val doidJson: String,
    val outputDirectory: String
) {

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(CLINICAL_JSON, true, "The clinical JSON of the patient for which a sample is analyzed")
            options.addOption(ORANGE_JSON, true, "(Optional) path of the ORANGE json to be interpreted")
            options.addOption(SERVE_DIRECTORY, true, "Path towards the SERVE directory containing known and actionable events")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where molecular data output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): MolecularInterpreterConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return MolecularInterpreterConfig(
                clinicalJson = ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON),
                orangeJson = ApplicationConfig.optionalFile(cmd, ORANGE_JSON),
                serveDirectory = ApplicationConfig.nonOptionalDir(cmd, SERVE_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }

        private val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterConfig::class.java)

        private const val CLINICAL_JSON: String = "clinical_json"
        private const val ORANGE_JSON: String = "orange_json"
        private const val SERVE_DIRECTORY: String = "serve_directory"
        private const val DOID_JSON: String = "doid_json"
        private const val OUTPUT_DIRECTORY: String = "output_directory"
        private const val LOG_DEBUG: String = "log_debug"
    }
}
