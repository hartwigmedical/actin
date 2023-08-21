package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.Virus
import java.util.stream.Stream

class MolecularDriversInterpreter(
    private val molecularDrivers: MolecularDrivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {
    fun filteredVariants(): Stream<Variant> {
        return streamAndFilterDrivers(molecularDrivers.variants())
    }

    fun filteredCopyNumbers(): Stream<CopyNumber> {
        return streamAndFilterDrivers(molecularDrivers.copyNumbers())
    }

    fun filteredHomozygousDisruptions(): Stream<HomozygousDisruption> {
        return streamAndFilterDrivers(molecularDrivers.homozygousDisruptions())
    }

    fun filteredDisruptions(): Stream<Disruption> {
        return streamAndFilterDrivers(molecularDrivers.disruptions())
    }

    fun filteredFusions(): Stream<Fusion> {
        return streamAndFilterDrivers(molecularDrivers.fusions())
    }

    fun filteredViruses(): Stream<Virus> {
        return streamAndFilterDrivers(molecularDrivers.viruses())
    }

    fun hasPotentiallySubClonalVariants(): Boolean {
        return filteredVariants().anyMatch { obj: Variant? -> ClonalityInterpreter.isPotentiallySubclonal() }
    }

    fun trialsForDriver(driver: Driver): List<String?>? {
        return evaluatedCohortsInterpreter.trialsForDriver(driver)
    }

    private fun <T : Driver?> streamAndFilterDrivers(drivers: Set<T>): Stream<T> {
        return drivers.stream().filter { driver: T -> driver!!.isReportable || evaluatedCohortsInterpreter.driverIsActionable(driver) }
    }
}