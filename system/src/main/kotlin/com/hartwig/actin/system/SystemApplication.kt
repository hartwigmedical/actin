package com.hartwig.actin.system

import com.hartwig.actin.algo.TreatmentMatcherApplication
import com.hartwig.actin.database.algo.TreatmentMatchLoaderApplication
import com.hartwig.actin.database.clinical.ClinicalLoaderApplication
import com.hartwig.actin.database.molecular.MolecularLoaderApplication
import com.hartwig.actin.database.trial.TrialLoaderApplication
import com.hartwig.actin.molecular.MolecularInterpreterApplication
import com.hartwig.actin.report.ReporterApplication
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SystemApplication {
    val LOGGER: Logger = LogManager.getLogger(SystemApplication::class.java)
    val VERSION = SystemApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
}

fun main() {
    SystemApplication.LOGGER.info("The following applications are available through ACTIN v{}", SystemApplication.VERSION)
    listOf(
        MolecularInterpreterApplication::class,
        TreatmentMatcherApplication::class,
        ClinicalLoaderApplication::class,
        MolecularLoaderApplication::class,
        TrialLoaderApplication::class,
        TreatmentMatchLoaderApplication::class,
        ReporterApplication::class
    ).forEach { applicationClass -> SystemApplication.LOGGER.info(" {}", applicationClass.java) }
}
