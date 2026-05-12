package com.hartwig.actin.system

import com.hartwig.actin.algo.TreatmentMatcherApplication
import com.hartwig.actin.database.algo.TreatmentMatchLoaderApplication
import com.hartwig.actin.database.clinical.ClinicalLoaderApplication
import com.hartwig.actin.database.molecular.MolecularLoaderApplication
import com.hartwig.actin.database.trial.TrialLoaderApplication
import com.hartwig.actin.molecular.MolecularInterpreterApplication
import com.hartwig.actin.report.ReporterApplication
import io.github.oshai.kotlinlogging.KotlinLogging

object SystemApplication {
    val logger = KotlinLogging.logger {}
    val VERSION = SystemApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
}

fun main() {
    SystemApplication.logger.info { "The following applications are available through ACTIN v${SystemApplication.VERSION}" }
    listOf(
        MolecularInterpreterApplication::class,
        TreatmentMatcherApplication::class,
        ClinicalLoaderApplication::class,
        MolecularLoaderApplication::class,
        TrialLoaderApplication::class,
        TreatmentMatchLoaderApplication::class,
        ReporterApplication::class
    ).forEach { applicationClass -> SystemApplication.logger.info { " ${applicationClass.java}" } }
}
