package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriverEntryComparatorTest {
    @Test
    fun canCompareMolecularDriverEntries() {
        val entry1 = create(DriverLikelihood.HIGH, "mutation", "driver 3")
        val entry2 = create(DriverLikelihood.HIGH, "amplification", "driver 4")
        val entry3 = create(DriverLikelihood.HIGH, "amplification", "driver 5")
        val entry4 = create(DriverLikelihood.HIGH, "loss", "driver 7")
        val entry5 = create(DriverLikelihood.HIGH, "fusion", "driver 7")
        val entry6 = create(DriverLikelihood.HIGH, "disruption", "driver 7")
        val entry7 = create(DriverLikelihood.HIGH, "virus", "driver 6")
        val entry8 = create(DriverLikelihood.MEDIUM, "fusion", "driver 1")
        val entry9 = create(DriverLikelihood.LOW, "disruption", "driver 2")
        val entries = listOf(entry8, entry6, entry3, entry1, entry9, entry2, entry7, entry4, entry5)
            .sortedWith(MolecularDriverEntryComparator())
       
        assertThat(entries[0]).isEqualTo(entry1)
        assertThat(entries[1]).isEqualTo(entry2)
        assertThat(entries[2]).isEqualTo(entry3)
        assertThat(entries[3]).isEqualTo(entry4)
        assertThat(entries[4]).isEqualTo(entry5)
        assertThat(entries[5]).isEqualTo(entry6)
        assertThat(entries[6]).isEqualTo(entry7)
        assertThat(entries[7]).isEqualTo(entry8)
        assertThat(entries[8]).isEqualTo(entry9)
    }

    companion object {
        private fun create(driverLikelihood: DriverLikelihood, driverType: String, driver: String): MolecularDriverEntry {
            return MolecularDriverEntry(driverType = driverType, driver = driver, driverLikelihood = driverLikelihood)
        }
    }
}