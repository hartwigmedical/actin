package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.trial.ExternalTrialSummary

object DriverTableFunctions {

    fun groupByEvent(externalTrialSummaries: Set<ExternalTrialSummary>): Map<String, Set<ExternalTrialSummary>> {
        return externalTrialSummaries.flatMap { summary -> summary.actinMolecularEvents.map { event -> event to summary } }
            .groupBy({ it.first }, { it.second }).mapValues { (_, value) -> value.toSet() }
    }

    fun allDrivers(molecularHistory: MolecularHistory): List<Pair<MolecularTest, List<Driver>>> =
        molecularHistory.molecularTests.map { it to allDrivers(it) }

    fun allDrivers(molecularTest: MolecularTest): List<Driver> =
        with(molecularTest.drivers) { variants + fusions + viruses + copyNumbers + disruptions }
}