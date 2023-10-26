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
import org.junit.Assert
import org.junit.Test

class DriverExtractorTest {
    @Test
    fun canExtractFromMinimalTestData() {
        val driverExtractor = createTestExtractor()
        val drivers = driverExtractor.extract(TestOrangeFactory.createMinimalTestOrangeRecord())
        Assert.assertEquals(0, drivers.variants().size.toLong())
        Assert.assertEquals(0, drivers.copyNumbers().size.toLong())
        Assert.assertEquals(0, drivers.homozygousDisruptions().size.toLong())
        Assert.assertEquals(0, drivers.disruptions().size.toLong())
        Assert.assertEquals(0, drivers.fusions().size.toLong())
        Assert.assertEquals(0, drivers.viruses().size.toLong())
    }

    @Test
    fun canExtractFromProperTestData() {
        val driverExtractor = createTestExtractor()
        val drivers = driverExtractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
        Assert.assertEquals(1, drivers.variants().size.toLong())
        Assert.assertEquals(2, drivers.copyNumbers().size.toLong())
        Assert.assertEquals(1, drivers.homozygousDisruptions().size.toLong())
        Assert.assertEquals(1, drivers.disruptions().size.toLong())
        Assert.assertEquals(1, drivers.fusions().size.toLong())
        Assert.assertEquals(1, drivers.viruses().size.toLong())
    }

    @Test
    fun canDetermineReportableLostGenes() {
        val copyNumbers: MutableList<CopyNumber> = Lists.newArrayList(TestCopyNumberFactory.builder().gene("gene 1").type(CopyNumberType.LOSS).isReportable(true).build(),
            TestCopyNumberFactory.builder().gene("gene 2").type(CopyNumberType.FULL_GAIN).isReportable(true).build(),
            TestCopyNumberFactory.builder().gene("gene 3").type(CopyNumberType.LOSS).isReportable(false).build())
        val lostGenes: MutableSet<String> = DriverExtractor.reportableLostGenes(copyNumbers)
        Assert.assertEquals(1, lostGenes.size.toLong())
        Assert.assertEquals("gene 1", lostGenes.iterator().next())
    }

    @Test
    fun canCountReportableDrivers() {
        val drivers: MutableList<Driver> = Lists.newArrayList(TestVariantFactory.builder().isReportable(true).build(),
            TestCopyNumberFactory.builder().isReportable(false).build(),
            TestFusionFactory.builder().isReportable(true).build(),
            TestVirusFactory.builder().isReportable(false).build())
        Assert.assertEquals(2, DriverExtractor.reportableCount(drivers).toLong())
        Assert.assertEquals(0, DriverExtractor.reportableCount(emptyList()).toLong())
    }

    companion object {
        private fun createTestExtractor(): DriverExtractor {
            return DriverExtractor.create(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase())
        }
    }
}