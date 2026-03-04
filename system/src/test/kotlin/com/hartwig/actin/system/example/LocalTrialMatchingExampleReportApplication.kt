package com.hartwig.actin.system.example

import org.apache.logging.log4j.LogManager

private const val TRIAL_EXAMPLE_TO_RUN = LUNG_01_EXAMPLE

object LocalTrialMatchingExampleReportApplication {
    val LOGGER = LogManager.getLogger(LocalTrialMatchingExampleReportApplication::class.java)
}

fun main() {
    LocalTrialMatchingExampleReportApplication.LOGGER.info("Running ACTIN Trial Example Reporter")
    ExampleFunctions.runExample(TRIAL_EXAMPLE_TO_RUN) { ExampleFunctions.createTrialMatchingReportConfiguration() }
}
