package com.hartwig.actin.system.example

import io.github.oshai.kotlinlogging.KotlinLogging

private const val TRIAL_EXAMPLE_TO_RUN = LUNG_01_EXAMPLE

object LocalTrialMatchingExampleReportApplication {
    val logger = KotlinLogging.logger {}
}

fun main() {
    LocalTrialMatchingExampleReportApplication.logger.info { "Running ACTIN Trial Example Reporter" }
    ExampleFunctions.runExample(TRIAL_EXAMPLE_TO_RUN) { ExampleFunctions.createTrialMatchingReportConfiguration() }
}
