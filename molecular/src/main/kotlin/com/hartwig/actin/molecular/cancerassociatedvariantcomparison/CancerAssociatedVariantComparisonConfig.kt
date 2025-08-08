package com.hartwig.actin.molecular.cancerassociatedvariantcomparison

import com.hartwig.actin.molecular.MolecularInterpreterConfig
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class CancerAssociatedVariantComparisonConfig(
    val orangeJson: String,
    val serveDirectory: String,
    val usedCombinedProfilesEfficacyEvidence: Boolean,
    val outputDirectory: String
) {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(CancerAssociatedVariantComparisonConfig::class.java)

        private const val ORANGE_JSON: String = "orange_json"
        private const val SERVE_DIRECTORY: String = "serve_directory"
        private const val USED_COMBINED_PROFILES_EFFICACY_EVIDENCE: String = "used_combined_profiles_efficacy_evidence"
        private const val OUTPUT_DIRECTORY: String = "output_directory"
        private const val LOG_DEBUG: String = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(ORANGE_JSON, true, "Path of the ORANGE json to be interpreted")
            options.addOption(SERVE_DIRECTORY, true, "Path towards the SERVE directory containing known events")
            options.addOption(USED_COMBINED_PROFILES_EFFICACY_EVIDENCE, false, "If set, the combined profiles for the efficacy evidence from the SERVE database where filtered out")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where cancer-associated variant comparison output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): CancerAssociatedVariantComparisonConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return CancerAssociatedVariantComparisonConfig(
                orangeJson = ApplicationConfig.nonOptionalFile(cmd, ORANGE_JSON),
                serveDirectory = ApplicationConfig.nonOptionalDir(cmd, SERVE_DIRECTORY),
                usedCombinedProfilesEfficacyEvidence = cmd.hasOption(USED_COMBINED_PROFILES_EFFICACY_EVIDENCE),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }
    }
}
