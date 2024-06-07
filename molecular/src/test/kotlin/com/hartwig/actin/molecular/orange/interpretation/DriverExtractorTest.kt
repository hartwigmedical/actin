package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverExtractorTest {

    @Test
    fun `Should extract from minimal test data`() {
        val driverExtractor = createTestExtractor()

        val drivers = driverExtractor.extract(TestOrangeFactory.createMinimalTestOrangeRecord())
        assertThat(drivers.variants).hasSize(0)
        assertThat(drivers.copyNumbers).hasSize(0)
        assertThat(drivers.homozygousDisruptions).hasSize(0)
        assertThat(drivers.disruptions).hasSize(0)
        assertThat(drivers.fusions).hasSize(0)
        assertThat(drivers.viruses).hasSize(0)
    }

    @Test
    fun `Should extract from proper test data`() {
        val driverExtractor = createTestExtractor()

        val drivers = driverExtractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
        assertThat(drivers.variants).hasSize(1)
        assertThat(drivers.copyNumbers).hasSize(3)
        assertThat(drivers.homozygousDisruptions).hasSize(1)
        assertThat(drivers.disruptions).hasSize(1)
        assertThat(drivers.fusions).hasSize(1)
        assertThat(drivers.viruses).hasSize(1)
    }

    @Test
    fun `Should determine reportable lost genes`() {
        val copyNumbers = listOf(
            TestCopyNumberFactory.createMinimal().copy(gene = "gene 1", type = CopyNumberType.LOSS, isReportable = true),
            TestCopyNumberFactory.createMinimal().copy(gene = "gene 2", type = CopyNumberType.FULL_GAIN, isReportable = true),
            TestCopyNumberFactory.createMinimal().copy(gene = "gene 3", type = CopyNumberType.LOSS, isReportable = false)
        )
        val lostGenes = DriverExtractor.reportableLostGenes(copyNumbers)
        assertThat(lostGenes).hasSize(1)
        assertThat(lostGenes.first()).isEqualTo("gene 1")
    }

    @Test
    fun `Should count reportable drivers`() {
        val drivers = listOf(
            TestVariantFactory.createMinimal().copy(isReportable = true),
            TestCopyNumberFactory.createMinimal().copy(isReportable = false),
            TestFusionFactory.createMinimal().copy(isReportable = true),
            TestVirusFactory.createMinimal().copy(isReportable = false)
        )
        assertThat(DriverExtractor.reportableCount(drivers)).isEqualTo(2)
        assertThat(DriverExtractor.reportableCount(emptyList())).isEqualTo(0)
    }

    companion object {
        private fun createTestExtractor(): DriverExtractor {
            return DriverExtractor.create(TestGeneFilterFactory.createAlwaysValid())
        }
    }
}