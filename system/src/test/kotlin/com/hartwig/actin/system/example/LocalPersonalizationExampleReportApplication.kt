package com.hartwig.actin.system.example

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.Locale

private const val PERSONALIZATION_EXAMPLE_TO_RUN = CRC_01_EXAMPLE

object LocalPersonalizationExampleReportApplication {
    val LOGGER: Logger = LogManager.getLogger(LocalPersonalizationExampleReportApplication::class.java)
}

fun main() {
    Locale.setDefault(Locale.US)

    LocalPersonalizationExampleReportApplication.LOGGER.info("Running ACTIN Personalization Example Reporter")
    ExampleFunctions.runExample(PERSONALIZATION_EXAMPLE_TO_RUN) { ExampleFunctions.createPersonalizationEnvironmentConfiguration() }
}