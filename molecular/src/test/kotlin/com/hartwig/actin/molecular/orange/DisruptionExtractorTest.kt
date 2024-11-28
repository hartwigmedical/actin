package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.orange.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.orange.driver.DisruptionType
import com.hartwig.actin.datamodel.molecular.orange.driver.RegionType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory.createMinimalTestOrangeRecord
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory.breakendBuilder
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory.structuralVariantBuilder
import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType
import com.hartwig.hmftools.datamodel.linx.LinxDriverType
import com.hartwig.hmftools.datamodel.linx.LinxRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.assertj.core.data.Offset
import org.junit.Test

private const val EPSILON = 1.0E-10

class DisruptionExtractorTest {

    private val extractor = DisruptionExtractor(TestGeneFilterFactory.createAlwaysValid())

    @Test
    fun `Should extract breakends`() {
        val structuralVariant1 = structuralVariantBuilder().svId(1).clusterId(5).build()
        val linxBreakend = breakendBuilder()
            .gene("gene 1")
            .reported(true)
            .isCanonical(true)
            .type(LinxBreakendType.DUP)
            .junctionCopyNumber(0.2)
            .undisruptedCopyNumber(1.6)
            .regionType(TranscriptRegionType.EXONIC)
            .codingType(TranscriptCodingType.CODING)
            .svId(1)
            .build()

        val linx = ImmutableLinxRecord.builder()
            .from(createMinimalTestOrangeRecord().linx())
            .addAllSomaticStructuralVariants(structuralVariant1)
            .addAllSomaticBreakends(linxBreakend)
            .build()

        val disruptions = extractor.extractDisruptions(linx, emptySet(), listOf())
        assertThat(disruptions).hasSize(1)

        val disruption = disruptions.iterator().next()
        assertThat(disruption.isReportable).isTrue
        assertThat(disruption.driverLikelihood).isEqualTo(DriverLikelihood.LOW)
        assertThat(disruption.type).isEqualTo(DisruptionType.DUP)
        assertThat(disruption.junctionCopyNumber).isEqualTo(0.2, Offset.offset(EPSILON))
        assertThat(disruption.undisruptedCopyNumber).isEqualTo(1.6, Offset.offset(EPSILON))
        assertThat(disruption.regionType).isEqualTo(RegionType.EXONIC)
        assertThat(disruption.codingContext).isEqualTo(CodingContext.CODING)
        assertThat(disruption.clusterGroup).isEqualTo(5)
    }

    @Test
    fun `Should only extract disruptions on canonical transcripts`() {
        val structuralVariant = structuralVariantBuilder().svId(1).clusterId(5).build()
        val canonical = breakendBuilder().gene("gene 1").svId(1).isCanonical(true).build()
        val nonCanonical = breakendBuilder().gene("gene 2").svId(1).isCanonical(false).build()

        val linx = ImmutableLinxRecord.builder()
            .from(createMinimalTestOrangeRecord().linx())
            .addAllSomaticStructuralVariants(structuralVariant)
            .addAllSomaticBreakends(canonical, nonCanonical)
            .build()

        val disruptions = extractor.extractDisruptions(linx, emptySet(), listOf())
        assertThat(disruptions).hasSize(1)

        val disruption = disruptions.iterator().next()
        assertThat(disruption.gene).isEqualTo("gene 1")
    }

    @Test
    fun `Should throw exception when filtering reported disruption`() {
        val linxBreakend = breakendBuilder().gene("gene 1").reported(true).isCanonical(true).build()
        val linx = ImmutableLinxRecord.builder()
            .from(createMinimalTestOrangeRecord().linx())
            .addAllSomaticBreakends(linxBreakend)
            .build()

        val neverValidExtractor = DisruptionExtractor(TestGeneFilterFactory.createNeverValid())

        assertThatIllegalStateException().isThrownBy { neverValidExtractor.extractDisruptions(linx, emptySet(), emptyList()) }
    }

    @Test
    fun `Should filter breakend with losses`() {
        val gene = "gene"

        val breakend1 = breakendBuilder().gene(gene).type(LinxBreakendType.DEL).isCanonical(true).build()
        assertThat(extractor.extractDisruptions(withBreakend(breakend1), setOf(gene), listOf())).hasSize(0)

        val breakend2 = breakendBuilder().gene(gene).type(LinxBreakendType.DUP).isCanonical(true).build()
        assertThat(extractor.extractDisruptions(withBreakend(breakend2), setOf(gene), listOf())).hasSize(1)

        val breakend3 = breakendBuilder().gene("other").type(LinxBreakendType.DEL).isCanonical(true).build()
        assertThat(extractor.extractDisruptions(withBreakend(breakend3), setOf(gene), listOf())).hasSize(1)
    }

    @Test
    fun `Should determine all disruption types`() {
        for (breakendType in LinxBreakendType.values()) {
            assertThat(extractor.determineDisruptionType(breakendType)).isNotNull()
        }
    }

    @Test
    fun `Should determine all region types`() {
        for (regionType in TranscriptRegionType.values()) {
            if (regionType != TranscriptRegionType.UNKNOWN) {
                assertThat(extractor.determineRegionType(regionType)).isNotNull()
            }
        }
    }

    @Test
    fun `Should determine all coding types`() {
        for (codingType in TranscriptCodingType.values()) {
            if (codingType != TranscriptCodingType.UNKNOWN) {
                assertThat(extractor.determineCodingContext(codingType)).isNotNull()
            }
        }
    }

    @Test
    fun `Should generate undisrupted copy number for hom dup disruptions`() {
        val linxBreakend = breakendBuilder()
            .gene("gene")
            .reported(true)
            .isCanonical(true)
            .type(LinxBreakendType.DUP)
            .junctionCopyNumber(1.2)
            .undisruptedCopyNumber(1.4)
            .svId(1)
            .build()
        val structuralVariant1 = structuralVariantBuilder().svId(1).clusterId(2).build()
        val linx = ImmutableLinxRecord.builder()
            .from(createMinimalTestOrangeRecord().linx())
            .addAllSomaticStructuralVariants(structuralVariant1)
            .addAllSomaticBreakends(linxBreakend)
            .build()
        val driver = TestLinxFactory.driverBuilder().gene("gene").type(LinxDriverType.HOM_DUP_DISRUPTION).build()

        val disruptions = extractor.extractDisruptions(linx, emptySet(), listOf(driver))
        val disruption = disruptions.first()

        assertThat(disruption.undisruptedCopyNumber).isEqualTo(0.2, Offset.offset(EPSILON))
    }

    private fun withBreakend(breakend: LinxBreakend): LinxRecord {
        return ImmutableLinxRecord.builder()
            .from(createMinimalTestOrangeRecord().linx())
            .addAllSomaticStructuralVariants(structuralVariantBuilder().svId(breakend.svId()).build())
            .addAllSomaticBreakends(breakend)
            .build()
    }
}