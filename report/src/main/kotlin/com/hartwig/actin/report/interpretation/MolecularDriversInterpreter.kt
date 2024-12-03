package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus

class MolecularDriversInterpreter(
    private val drivers: Drivers,
    private val interpretedCohortsSummarizer: InterpretedCohortsSummarizer
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
        return interpretedCohortsSummarizer.trialsForDriver(driver)
    }

    private fun <T : Driver> filterDrivers(drivers: Collection<T>): List<T> {
        return drivers.filter { it.isReportable || interpretedCohortsSummarizer.driverIsActionable(it) }
    }
}