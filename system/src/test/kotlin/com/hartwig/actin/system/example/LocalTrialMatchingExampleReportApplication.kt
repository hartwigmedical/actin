package com.hartwig.actin.system.example

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.Locale

private const val TRIAL_EXAMPLE_TO_RUN = LUNG_01_EXAMPLE

object LocalTrialMatchingExampleReportApplication {
    val LOGGER: Logger = LogManager.getLogger(LocalTrialMatchingExampleReportApplication::class.java)
}

fun main() {
    Locale.setDefault(Locale.US)

    LocalTrialMatchingExampleReportApplication.LOGGER.info("Running ACTIN Trial Example Reporter")
    ExampleFunctions.runExample(TRIAL_EXAMPLE_TO_RUN) { ExampleFunctions.createTrialMatchingEnvironmentConfiguration() }
}
