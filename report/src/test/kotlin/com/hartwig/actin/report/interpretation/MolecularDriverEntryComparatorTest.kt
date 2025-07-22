package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriverEntryComparatorTest {

    @Test
    fun `Should sort molecular driver entries`() {
        val expectedEntries = listOf(
            create(DriverLikelihood.HIGH, "mutation", "driver 3"),
            create(DriverLikelihood.HIGH, "mutation", "driver 3"),
            create(DriverLikelihood.HIGH, "amplification", "driver 4"),
            create(DriverLikelihood.HIGH, "amplification", "driver 5"),
            create(DriverLikelihood.HIGH, "deletion", "driver 7"),
            create(DriverLikelihood.HIGH, "fusion", "driver 7"),
            create(DriverLikelihood.HIGH, "disruption", "driver 7"),
            create(DriverLikelihood.HIGH, "virus", "driver 6"),
            create(DriverLikelihood.MEDIUM, "fusion", "driver 1"),
            create(DriverLikelihood.LOW, "disruption", "driver 2"),
        )

        val entries = listOf(
            expectedEntries[8],
            expectedEntries[6],
            expectedEntries[3],
            expectedEntries[0],
            expectedEntries[1],
            expectedEntries[9],
            expectedEntries[2],
            expectedEntries[7],
            expectedEntries[4],
            expectedEntries[5]
        ).sortedWith(MolecularDriverEntryComparator())

        expectedEntries.indices.forEach {
            assertThat(entries[it]).isEqualTo(expectedEntries[it])
        }
    }

    private fun create(driverLikelihood: DriverLikelihood, driverType: String, driver: String): MolecularDriverEntry {
        return MolecularDriverEntry(
            driverType = driverType,
            description = driver,
            event = driver,
            driverLikelihood = driverLikelihood,
            evidenceTier = EvidenceTier.I
        )
    }
}