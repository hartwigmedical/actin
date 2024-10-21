package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial

object DriverTableFunctions {
    fun groupByEvent(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>): Map<String, Iterable<ExternalTrial>> {
        return externalTrialsPerEvent.flatMap { (key, value) -> key.split(",").map { it.trim() to value } }
            .groupBy({ it.first }, { it.second }).mapValues { (_, trials) -> trials.flatten().toSet() }
    }

    fun allDrivers(molecularHistory: MolecularHistory): List<Pair<MolecularTest, Set<Driver>>> =
        molecularHistory.molecularTests.map { it to allDrivers(it) }

    fun allDrivers(molecularTest: MolecularTest): Set<Driver> =
        with(molecularTest.drivers) { variants + fusions + viruses + copyNumbers + disruptions }
}