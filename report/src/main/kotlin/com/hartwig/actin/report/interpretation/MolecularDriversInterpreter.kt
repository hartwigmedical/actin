package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus

class MolecularDriversInterpreter(
    private val drivers: Drivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {
    fun filteredVariants(): List<Variant> {
        return filterDrivers(drivers.variants)
    }

    fun filteredCopyNumbers(): List<CopyNumber> {
        return filterDrivers(drivers.copyNumbers)
    }

    fun filteredHomozygousDisruptions(): List<HomozygousDisruption> {
        return filterDrivers(drivers.homozygousDisruptions)
    }

    fun filteredDisruptions(): List<Disruption> {
        return filterDrivers(drivers.disruptions)
    }

    fun filteredFusions(): List<Fusion> {
        return filterDrivers(drivers.fusions)
    }

    fun filteredViruses(): List<Virus> {
        return filterDrivers(drivers.viruses)
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