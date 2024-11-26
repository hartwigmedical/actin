package com.hartwig.actin.algo

import com.hartwig.actin.configuration.OVERRIDE_YAML_ARGUMENT
import com.hartwig.actin.configuration.OVERRIDE_YAML_DESCRIPTION
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class TreatmentMatcherConfig(
    val patientRecordJson: String,
    val trialDatabaseDirectory: String,
    val treatmentDirectory: String,
    val doidJson: String,
    val atcTsv: String,
    val extendedEfficacyJson: String,
    val personalizationDataPath: String?,
    val serveDirectory: String,
    val outputDirectory: String,
    val runHistorically: Boolean,
    val overridesYaml: String?
) {

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherConfig::class.java)

        private const val PATIENT_RECORD_JSON = "patient_json"
        private const val TRIAL_DATABASE_DIRECTORY = "trial_database_directory"
        private const val TREATMENT_DIRECTORY = "treatment_directory"
        private const val DOID_JSON = "doid_json"
        private const val ATC_TSV = "atc_tsv"
        private const val EXTENDED_EFFICACY_JSON = "extended_efficacy_json"
        private const val PERSONALIZATION_DATA_PATH = "personalization_data_path"
        private const val SERVE_DIRECTORY: String = "serve_directory"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val RUN_HISTORICALLY = "run_historically"
        private const val TRIAL_SOURCE = "trial_source"
        private const val LOG_DEBUG = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(PATIENT_RECORD_JSON, true, "File containing the patient record")
            options.addOption(TRIAL_DATABASE_DIRECTORY, true, "Directory containing all available trials")
            options.addOption(TREATMENT_DIRECTORY, true, "Path to treatment data directory")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(ATC_TSV, true, "Path to TSV file container the full ATC tree")
            options.addOption(EXTENDED_EFFICACY_JSON, true, "Path to JSON file containing extended efficacy evidence")
            options.addOption(PERSONALIZATION_DATA_PATH, true, "Path to personalization data file")
            options.addOption(SERVE_DIRECTORY, true, "Path towards the SERVE directory containing known and actionable events")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where the matcher output will be written to")
            options.addOption(
                RUN_HISTORICALLY,
                false,
                "If set, runs the algo with a date just after the original patient registration date"
            )
            options.addOption(
                TRIAL_SOURCE,
                true,
                "Hospital managing trials provided. Currently only a single hospital is supported, and defaults to EMC"
            )
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            options.addOption(OVERRIDE_YAML_ARGUMENT, true, OVERRIDE_YAML_DESCRIPTION)
            return options
        }

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
                patientRecordJson = ApplicationConfig.nonOptionalFile(cmd, PATIENT_RECORD_JSON),
                trialDatabaseDirectory = ApplicationConfig.nonOptionalDir(cmd, TRIAL_DATABASE_DIRECTORY),
                treatmentDirectory = ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                atcTsv = ApplicationConfig.nonOptionalFile(cmd, ATC_TSV),
                extendedEfficacyJson = ApplicationConfig.nonOptionalFile(cmd, EXTENDED_EFFICACY_JSON),
                personalizationDataPath = ApplicationConfig.optionalFile(cmd, PERSONALIZATION_DATA_PATH),
                serveDirectory = "/home/cbruel/",
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
                runHistorically = runHistorically,
                overridesYaml = ApplicationConfig.optionalFile(cmd, OVERRIDE_YAML_ARGUMENT)
            )
        }
    }
}