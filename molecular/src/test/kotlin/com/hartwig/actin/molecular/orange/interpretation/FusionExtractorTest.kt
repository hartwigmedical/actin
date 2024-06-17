package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionExtractorTest {

    @Test
    fun `Should extract fusions`() {
        val linxFusion = TestLinxFactory.fusionBuilder()
            .reported(true)
            .reportedType(LinxFusionType.PROMISCUOUS_5)
            .geneStart("gene start")
            .geneTranscriptStart("trans start")
            .fusedExonUp(1)
            .geneEnd("gene end")
            .geneTranscriptEnd("trans end")
            .fusedExonDown(4)
            .driverLikelihood(FusionLikelihoodType.HIGH)
            .build()

        val linx = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticFusions(linxFusion)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("gene end")
        val fusionExtractor = FusionExtractor(geneFilter)

        val fusions = fusionExtractor.extract(linx)
        assertThat(fusions.size.toLong()).isEqualTo(1)

        val fusion = fusions.iterator().next()
        assertThat(fusion.isReportable).isTrue
        assertThat(fusion.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(fusion.geneStart).isEqualTo("gene start")
        assertThat(fusion.geneTranscriptStart).isEqualTo("trans start")
        assertThat(fusion.extendedFusion?.fusedExonUp?.toLong()).isEqualTo(1)
        assertThat(fusion.geneEnd).isEqualTo("gene end")
        assertThat(fusion.geneTranscriptEnd).isEqualTo("trans end")
        assertThat(fusion.extendedFusion?.fusedExonDown?.toLong()).isEqualTo(4)
        assertThat(fusion.driverType).isEqualTo(FusionDriverType.PROMISCUOUS_5)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when filtering reported fusion`() {
        val linxFusion = TestLinxFactory.fusionBuilder().reported(true).geneStart("other start").geneEnd("other end").build()
        val linx = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticFusions(linxFusion)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val fusionExtractor = FusionExtractor(geneFilter)
        fusionExtractor.extract(linx)
    }

    @Test
    fun `Should determine driver type for all fusions`() {
        for (type in LinxFusionType.values()) {
            val fusion: LinxFusion = TestLinxFactory.fusionBuilder().reportedType(type).build()
            assertThat(FusionExtractor.determineDriverType(fusion)).isNotNull()
        }
    }

    @Test
    fun `Should determine driver likelihood for all fusions`() {
        val high = TestLinxFactory.fusionBuilder().driverLikelihood(FusionLikelihoodType.HIGH).build()
        assertThat(FusionExtractor.determineDriverLikelihood(high)).isEqualTo(DriverLikelihood.HIGH)

        val low = TestLinxFactory.fusionBuilder().driverLikelihood(FusionLikelihoodType.LOW).build()
        assertThat(FusionExtractor.determineDriverLikelihood(low)).isEqualTo(DriverLikelihood.LOW)

        val na = TestLinxFactory.fusionBuilder().driverLikelihood(FusionLikelihoodType.NA).build()
        assertThat(FusionExtractor.determineDriverLikelihood(na)).isNull()
    }
}