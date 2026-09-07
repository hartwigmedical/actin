package com.hartwig.actin.report

import com.hartwig.actin.configuration.OVERRIDE_YAML_ARGUMENT
import com.hartwig.actin.configuration.OVERRIDE_YAML_DESCRIPTION
import com.hartwig.actin.util.ApplicationConfig
import com.hartwig.actin.utils.enableDebugLogging
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options

data class ReporterConfig(
    val patientJson: String,
    val treatmentMatchJson: String,
    val doidJson: String,
    val overrideYaml: String,
    val outputDirectory: String,
    val reportDate: LocalDate?,
    val itextLicenseKey: String?
) {

    companion object {
        val logger = KotlinLogging.logger {}

        private const val PATIENT_JSON = "patient_json"
        private const val TREATMENT_MATCH_JSON = "treatment_match_json"
        private const val DOID_JSON = "doid_json"
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val LOG_DEBUG = "log_debug"
        private const val REPORT_DATE = "report_date"
        private const val ITEXT_LICENSE_KEY = "itext_license_key"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(PATIENT_JSON, true, "File containing the patient record")
            options.addOption(TREATMENT_MATCH_JSON, true, "File containing the result of the ACTIN treatment matcher algo")
            options.addOption(DOID_JSON, true, "File containing the DOID model")
            options.addOption(OVERRIDE_YAML_ARGUMENT, true, OVERRIDE_YAML_DESCRIPTION)
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where the report will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            options.addOption(REPORT_DATE, true, "If set, sets fixed report date")
            options.addOption(ITEXT_LICENSE_KEY, true, "File containing the iText license key")
            return options
        }

        fun createConfig(cmd: CommandLine): ReporterConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                enableDebugLogging()
            }

            return ReporterConfig(
                patientJson = ApplicationConfig.nonOptionalFile(cmd, PATIENT_JSON),
                treatmentMatchJson = ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                overrideYaml = ApplicationConfig.nonOptionalFile(cmd, OVERRIDE_YAML_ARGUMENT),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY),
                reportDate = cmd.getOptionValue(REPORT_DATE)?.let { LocalDate.parse(it) },
                itextLicenseKey = ApplicationConfig.optionalFile(cmd, ITEXT_LICENSE_KEY)
            )
        }
    }
}