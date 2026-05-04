package com.hartwig.actin.algo

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options

data class EligibilityRuleExportConfig(
    val outputDirectory: String
) {

    companion object {
        private const val OUTPUT_DIRECTORY = "output_directory"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where the eligibility rule JSON will be written to")
            return options
        }

        fun createConfig(cmd: CommandLine): EligibilityRuleExportConfig {

            return EligibilityRuleExportConfig(
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }
    }
}
