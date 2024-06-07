package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusion
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariant
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus

class MolecularDriversInterpreter(
    private val molecularDrivers: MolecularDrivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {
    fun filteredVariants(): List<ExtendedVariant> {
        return filterDrivers(molecularDrivers.variants)
    }

    fun filteredCopyNumbers(): List<CopyNumber> {
        return filterDrivers(molecularDrivers.copyNumbers)
    }

    fun filteredHomozygousDisruptions(): List<HomozygousDisruption> {
        return filterDrivers(molecularDrivers.homozygousDisruptions)
    }

    fun filteredDisruptions(): List<Disruption> {
        return filterDrivers(molecularDrivers.disruptions)
    }

    fun filteredFusions(): List<ExtendedFusion> {
        return filterDrivers(molecularDrivers.fusions)
    }

    fun filteredViruses(): List<Virus> {
        return filterDrivers(molecularDrivers.viruses)
    }

    fun hasPotentiallySubClonalVariants(): Boolean {
        return filteredVariants().any(ClonalityInterpreter::isPotentiallySubclonal)
    }

    fun trialsForDriver(driver: Driver): List<String> {
        return evaluatedCohortsInterpreter.trialsForDriver(driver)
    }

    private fun <T : Driver> filterDrivers(drivers: Set<T>): List<T> {
        return drivers.filter { it.isReportable || evaluatedCohortsInterpreter.driverIsActionable(it) }
    }
}