package com.hartwig.actin.configuration

import org.junit.Test

class EnvironmentConfigurationPrinterTest {

    @Test
    fun `Should print default environment configuration`() {
        val defaultConfig = EnvironmentConfiguration()

        EnvironmentConfigurationPrinter.printAlgoConfig(defaultConfig.algo)
        EnvironmentConfigurationPrinter.printTrialConfig(defaultConfig.trial)
        EnvironmentConfigurationPrinter.printReportConfig(defaultConfig.report)
    }
}
