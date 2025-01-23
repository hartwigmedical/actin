package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverExtractorTest {

    private val extractor = DriverExtractor.create(TestGeneFilterFactory.createAlwaysValid())

    @Test
    fun `Should extract from minimal test data`() {
        val drivers = extractor.extract(TestOrangeFactory.createMinimalTestOrangeRecord())
        assertThat(drivers.variants).hasSize(0)
        assertThat(drivers.copyNumbers).hasSize(0)
        assertThat(drivers.homozygousDisruptions).hasSize(0)
        assertThat(drivers.disruptions).hasSize(0)
        assertThat(drivers.fusions).hasSize(0)
        assertThat(drivers.viruses).hasSize(0)
    }

    @Test
    fun `Should extract from proper test data`() {
        val drivers = extractor.extract(TestOrangeFactory.createProperTestOrangeRecord())
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
            TestCopyNumberFactory.createMinimal().copy(
                gene = "gene 1",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS),
                isReportable = true
            ),
            TestCopyNumberFactory.createMinimal().copy(
                gene = "gene 2",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN),
                isReportable = true
            ),
            TestCopyNumberFactory.createMinimal().copy(
                gene = "gene 3",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS),
                isReportable = false
            )
        )
        val lostGenes = extractor.reportableLostGenes(copyNumbers)
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
        assertThat(extractor.reportableCount(drivers)).isEqualTo(2)
        assertThat(extractor.reportableCount(emptyList())).isEqualTo(0)
    }
}