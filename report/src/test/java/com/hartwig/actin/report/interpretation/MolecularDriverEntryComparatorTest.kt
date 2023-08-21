package com.hartwig.actin.report.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import org.junit.Assert
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
        val entries: List<MolecularDriverEntry> = Lists.newArrayList(entry8, entry6, entry3, entry1, entry9, entry2, entry7, entry4, entry5)
        entries.sort(MolecularDriverEntryComparator())
        Assert.assertEquals(entry1, entries[0])
        Assert.assertEquals(entry2, entries[1])
        Assert.assertEquals(entry3, entries[2])
        Assert.assertEquals(entry4, entries[3])
        Assert.assertEquals(entry5, entries[4])
        Assert.assertEquals(entry6, entries[5])
        Assert.assertEquals(entry7, entries[6])
        Assert.assertEquals(entry8, entries[7])
        Assert.assertEquals(entry9, entries[8])
    }

    companion object {
        private fun create(
            driverLikelihood: DriverLikelihood, driverType: String,
            driver: String
        ): MolecularDriverEntry {
            return ImmutableMolecularDriverEntry.builder().driverType(driverType).driver(driver).driverLikelihood(driverLikelihood).build()
        }
    }
}