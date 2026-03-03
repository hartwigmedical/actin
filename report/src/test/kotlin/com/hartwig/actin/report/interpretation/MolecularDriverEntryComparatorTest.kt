package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MolecularDriverEntryComparatorTest {

    @Test
    fun `Should sort molecular driver entries`() {
        val expectedEntries = listOf(
            create(DriverLikelihood.HIGH, "mutation", "driver3"),
            create(DriverLikelihood.HIGH, "mutation", "driver3"),
            create(DriverLikelihood.HIGH, "amplification", "driver4"),
            create(DriverLikelihood.HIGH, "amplification", "driver5"),
            create(DriverLikelihood.HIGH, "deletion", "driver7"),
            create(DriverLikelihood.HIGH, "fusion", "driver7"),
            create(DriverLikelihood.HIGH, "disruption", "driver7"),
            create(DriverLikelihood.HIGH, "virus", "driver6"),
            create(DriverLikelihood.MEDIUM, "fusion", "driver1"),
            create(DriverLikelihood.LOW, "disruption", "driver2"),
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

    @Test
    fun `Should sort cancer associated variants above no cancer associated variants`() {
        val expectedEntries = listOf(
            create(DriverLikelihood.HIGH, "mutation (loss of function)", "driverA"),
            create(DriverLikelihood.HIGH, "mutation (no known cancer-associated variant)", "driverA"),
            create(DriverLikelihood.HIGH, "mutation", "driverB"),
            create(DriverLikelihood.HIGH, "mutation (no known cancer-associated variant)", "driverC"),
        )

        val entries = listOf(
            expectedEntries[3],
            expectedEntries[0],
            expectedEntries[2],
            expectedEntries[1],
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