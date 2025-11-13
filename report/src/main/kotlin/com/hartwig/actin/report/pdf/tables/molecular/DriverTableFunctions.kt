package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.report.trial.ActionableWithExternalTrial

object DriverTableFunctions {

    fun groupByEvent(externalTrialSummaries: Set<ActionableWithExternalTrial>): Map<String, String> {
        return externalTrialSummaries.groupBy { e -> e.actionable.event }
            .mapValues { entry -> entry.value.joinToString(", ") { it.trial.nctId } }
    }

    fun allDrivers(molecularTests: List<MolecularTest>): List<Pair<MolecularTest, List<Driver>>> =
        molecularTests.map { it to allDrivers(it) }

    fun allDrivers(molecularTest: MolecularTest): List<Driver> =
        with(molecularTest.drivers) { variants + fusions + viruses + copyNumbers + disruptions }
}