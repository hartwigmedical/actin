package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HomozygousDisruptionExtractorTest {

    @Test
    fun `Should extract homozygous disruptions`() {
        val linxHomDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
        val linx = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addSomaticHomozygousDisruptions(linxHomDisruption)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(linxHomDisruption.gene())
        val homDisruptionExtractor = HomozygousDisruptionExtractor(geneFilter)

        val homDisruptions = homDisruptionExtractor.extractHomozygousDisruptions(linx)
        assertThat(homDisruptions).hasSize(1)

        val homDisruption = homDisruptions.iterator().next()
        assertThat(homDisruption.isReportable).isTrue
        assertThat(homDisruption.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(homDisruption.gene).isEqualTo("gene 1")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when filtering reported homozygous disruption`() {
        val linxHomDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
        val linx = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addSomaticHomozygousDisruptions(linxHomDisruption)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("other gene")
        val homDisruptionExtractor = HomozygousDisruptionExtractor(geneFilter)
        homDisruptionExtractor.extractHomozygousDisruptions(linx)
    }
}