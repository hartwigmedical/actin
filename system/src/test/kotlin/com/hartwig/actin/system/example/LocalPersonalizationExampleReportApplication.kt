package com.hartwig.actin.system.example

import org.apache.logging.log4j.LogManager

private const val PERSONALIZATION_EXAMPLE_TO_RUN = CRC_01_EXAMPLE

object LocalPersonalizationExampleReportApplication {
    val LOGGER = LogManager.getLogger(LocalPersonalizationExampleReportApplication::class.java)
}

fun main() {
    LocalPersonalizationExampleReportApplication.LOGGER.info("Running ACTIN Personalization Example Reporter")
    ExampleFunctions.runExample(PERSONALIZATION_EXAMPLE_TO_RUN) { ExampleFunctions.createPersonalizationReportConfiguration() }
}