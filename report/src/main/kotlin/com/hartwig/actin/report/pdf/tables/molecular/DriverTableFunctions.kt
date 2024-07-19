package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

object DriverTableFunctions {
    fun groupByEvent(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>): Map<String, Iterable<ExternalTrial>> {
        return externalTrialsPerEvent.flatMap { (key, value) -> key.split(",").map { it.trim() to value } }
            .groupBy({ it.first }, { it.second }).mapValues { (_, trials) -> trials.flatten().toSet() }
    }

    fun allDrivers(molecularHistory: MolecularHistory): List<Pair<MolecularTest, Set<Driver>>> =
        molecularHistory.molecularTests.map { it to (it.drivers.variants + it.drivers.fusions + it.drivers.viruses + it.drivers.copyNumbers + it.drivers.disruptions) }
}