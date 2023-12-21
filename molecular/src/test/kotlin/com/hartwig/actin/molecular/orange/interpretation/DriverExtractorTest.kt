package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class DriverExtractorTest {

    @Test
    fun canExtractFromMinimalTestData() {
        val driverExtractor = createTestExtractor()

        val drivers = driverExtractor.extract(TestOrangeFactory.createMinimalTestOrangeRecord())
        assertEquals(0, drivers.variants().size.toLong())
        assertEquals(0, drivers.copyNumbers().size.toLong())
        assertEquals(0, drivers.homozygousDisruptions().size.toLong())
        assertEquals(0, drivers.disruptions().size.toLong())
        assertEquals(0, drivers.fusions().size.toLong())
        assertEquals(0, drivers.viruses().size.toLong())
    }

    @Test
    fun canExtractFromProperTestData() {
        val driverExtractor = createTestExtractor()

        val drivers = driverExtractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
        assertEquals(1, drivers.variants().size.toLong())
        assertEquals(2, drivers.copyNumbers().size.toLong())
        assertEquals(1, drivers.homozygousDisruptions().size.toLong())
        assertEquals(1, drivers.disruptions().size.toLong())
        assertEquals(1, drivers.fusions().size.toLong())
        assertEquals(1, drivers.viruses().size.toLong())
    }

    @Test
    fun canDetermineReportableLostGenes() {
        val copyNumbers: MutableList<CopyNumber> = Lists.newArrayList(
            TestCopyNumberFactory.createMinimal().gene("gene 1").type(CopyNumberType.LOSS).isReportable(true).build(),
            TestCopyNumberFactory.createMinimal().gene("gene 2").type(CopyNumberType.FULL_GAIN).isReportable(true).build(),
            TestCopyNumberFactory.createMinimal().gene("gene 3").type(CopyNumberType.LOSS).isReportable(false).build()
        )
        val lostGenes: MutableSet<String> = DriverExtractor.reportableLostGenes(copyNumbers)
        assertEquals(1, lostGenes.size.toLong())
        assertEquals("gene 1", lostGenes.iterator().next())
    }

    @Test
    fun canCountReportableDrivers() {
        val drivers: MutableList<Driver> = Lists.newArrayList(
            TestVariantFactory.createMinimal().isReportable(true).build(),
            TestCopyNumberFactory.createMinimal().isReportable(false).build(),
            TestFusionFactory.createMinimal().isReportable(true).build(),
            TestVirusFactory.createMinimal().isReportable(false).build()
        )
        assertEquals(2, DriverExtractor.reportableCount(drivers).toLong())
        assertEquals(0, DriverExtractor.reportableCount(emptyList()).toLong())
    }

    companion object {
        private fun createTestExtractor(): DriverExtractor {
            return DriverExtractor.create(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase())
        }
    }
}