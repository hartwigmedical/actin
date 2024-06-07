package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriverEntryComparatorTest {
    @Test
    fun canCompareMolecularDriverEntries() {
        val expectedEntries = listOf(
            create(DriverLikelihood.HIGH, "mutation", "driver 3"),
            create(DriverLikelihood.HIGH, "amplification", "driver 4"),
            create(DriverLikelihood.HIGH, "amplification", "driver 5"),
            create(DriverLikelihood.HIGH, "loss", "driver 7"),
            create(DriverLikelihood.HIGH, "fusion", "driver 7"),
            create(DriverLikelihood.HIGH, "disruption", "driver 7"),
            create(DriverLikelihood.HIGH, "virus", "driver 6"),
            create(DriverLikelihood.MEDIUM, "fusion", "driver 1"),
            create(DriverLikelihood.LOW, "disruption (A)", "driver 2"),
            create(DriverLikelihood.LOW, "disruption (B)", "driver 2")
        )
        val entries = listOf(
            expectedEntries[7],
            expectedEntries[5],
            expectedEntries[9],
            expectedEntries[2],
            expectedEntries[0],
            expectedEntries[8],
            expectedEntries[1],
            expectedEntries[6],
            expectedEntries[3],
            expectedEntries[4]
        ).sortedWith(MolecularDriverEntryComparator())

        expectedEntries.indices.forEach {
            assertThat(entries[it]).isEqualTo(expectedEntries[it])
        }
    }

    companion object {
        private fun create(driverLikelihood: DriverLikelihood, driverType: String, driver: String): MolecularDriverEntry {
            return MolecularDriverEntry(driverType = driverType, displayedName = driver, eventName = driver, driverLikelihood = driverLikelihood)
        }
    }
}