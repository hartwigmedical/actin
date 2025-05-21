package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.report.trial.EventWithExternalTrial

object DriverTableFunctions {

    fun groupByEvent(externalTrialSummaries: Set<EventWithExternalTrial>): Map<String, String> {
        return externalTrialSummaries.groupBy { e -> e.event }.mapValues { entry -> entry.value.joinToString(", ") { it.trial.nctId } }
    }

    fun allDrivers(molecularHistory: MolecularHistory): List<Pair<MolecularTest, List<Driver>>> =
        molecularHistory.molecularTests.map { it to allDrivers(it) }

    fun allDrivers(molecularTest: MolecularTest): List<Driver> =
        with(molecularTest.drivers) { variants + fusions + viruses + copyNumbers + disruptions }
}