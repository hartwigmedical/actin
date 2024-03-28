package com.hartwig.actin.algo.soc

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class StandardOfCareConfig(
    val clinicalJson: String, val molecularJson: String, val doidJson: String, val atcTsv: String, val treatmentDirectory: String,
    val runHistorically: Boolean, val overridesYaml: String?
) {
    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the patient")
            options.addOption(MOLECULAR_JSON, true, "File containing the most recent molecular record of the patient")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree")
            options.addOption(ATC_TSV, true, "Path to TSV file container the full ATC tree")
            options.addOption(TREATMENT_DIRECTORY, true, "Path to treatment data directory")
            options.addOption(
                RUN_HISTORICALLY,
                false,
                "If set, runs the algo with a date just after the original patient registration date"
            )
            options.addOption(OVERRIDES_YAML, true, "Path to optional configuration overrides YAML file")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): StandardOfCareConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            val runHistorically = cmd.hasOption(RUN_HISTORICALLY)
            if (runHistorically) {
                LOGGER.info("Configured to run in historic mode")
            }
            return StandardOfCareConfig(
                clinicalJson = ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON),
                molecularJson = ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                treatmentDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DIRECTORY),
                runHistorically = runHistorically,
                atcTsv = ApplicationConfig.nonOptionalFile(cmd, ATC_TSV),
                overridesYaml = ApplicationConfig.optionalFile(cmd, OVERRIDES_YAML)
            )
        }

        private val LOGGER: Logger = LogManager.getLogger(StandardOfCareConfig::class)
        private const val CLINICAL_JSON = "clinical_json"
        private const val MOLECULAR_JSON = "molecular_json"
        private const val DOID_JSON = "doid_json"
        private const val ATC_TSV = "atc_tsv"
        private const val TREATMENT_DIRECTORY = "treatment_directory"
        private const val RUN_HISTORICALLY = "run_historically"
        private const val LOG_DEBUG = "log_debug"
        private const val OVERRIDES_YAML = "overrides_yaml"
    }
}