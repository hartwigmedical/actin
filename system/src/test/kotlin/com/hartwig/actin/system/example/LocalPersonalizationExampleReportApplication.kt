package com.hartwig.actin.system.example

import io.github.oshai.kotlinlogging.KotlinLogging

private const val PERSONALIZATION_EXAMPLE_TO_RUN = CRC_01_EXAMPLE

object LocalPersonalizationExampleReportApplication {
    val logger = KotlinLogging.logger {}
}

fun main() {
    LocalPersonalizationExampleReportApplication.logger.info { "Running ACTIN Personalization Example Reporter" }
    ExampleFunctions.runExample(PERSONALIZATION_EXAMPLE_TO_RUN) { ExampleFunctions.createPersonalizationReportConfiguration() }
}