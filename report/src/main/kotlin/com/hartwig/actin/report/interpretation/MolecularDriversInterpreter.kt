package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Virus

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

    fun trialsForDriver(driver: Driver): List<TrialAcronymAndLocations> {
        return interpretedCohortsSummarizer.trialsForDriver(driver)
    }

    fun copyNumberIsActionable(copyNumber: CopyNumber): Boolean {
        return interpretedCohortsSummarizer.driverIsActionable(copyNumber)
    }

    private fun <T : Driver> filterDrivers(drivers: Collection<T>): List<T> {
        return drivers.filter { it.isReportable || interpretedCohortsSummarizer.driverIsActionable(it) }
    }
}