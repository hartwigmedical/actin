package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.RegionType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType
import com.hartwig.hmftools.datamodel.linx.LinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxSvAnnotation
import org.junit.Assert
import org.junit.Test

class DisruptionExtractorTest {
    @Test
    fun canExtractBreakends() {
        val structuralVariant1: LinxSvAnnotation = TestLinxFactory.structuralVariantBuilder().svId(1).clusterId(5).build()
        val linxBreakend: LinxBreakend = TestLinxFactory.breakendBuilder()
            .gene("gene 1")
            .reportedDisruption(true)
            .type(LinxBreakendType.DUP)
            .junctionCopyNumber(0.2)
            .undisruptedCopyNumber(1.6)
            .regionType(TranscriptRegionType.EXONIC)
            .codingType(TranscriptCodingType.CODING)
            .svId(1)
            .build()
        val linx: LinxRecord = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticStructuralVariants(structuralVariant1)
            .addAllSomaticBreakends(linxBreakend)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(linxBreakend.gene())
        val disruptionExtractor = DisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        val disruptions = disruptionExtractor.extractDisruptions(linx, Sets.newHashSet())
        Assert.assertEquals(1, disruptions.size.toLong())
        val disruption = disruptions.iterator().next()
        Assert.assertTrue(disruption.isReportable())
        Assert.assertEquals(DriverLikelihood.LOW, disruption.driverLikelihood())
        Assert.assertEquals(DisruptionType.DUP, disruption.type())
        Assert.assertEquals(0.2, disruption.junctionCopyNumber(), EPSILON)
        Assert.assertEquals(1.6, disruption.undisruptedCopyNumber(), EPSILON)
        Assert.assertEquals(RegionType.EXONIC, disruption.regionType())
        Assert.assertEquals(CodingContext.CODING, disruption.codingContext())
        Assert.assertEquals(5, disruption.clusterGroup().toLong())
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedDisruption() {
        val linxBreakend: LinxBreakend = TestLinxFactory.breakendBuilder().gene("gene 1").reportedDisruption(true).build()
        val linx: LinxRecord = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticBreakends(linxBreakend)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val disruptionExtractor = DisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        disruptionExtractor.extractDisruptions(linx, mutableSetOf())
    }

    @Test
    fun canFilterBreakendWithLosses() {
        val gene = "gene"
        val disruptionExtractor = DisruptionExtractor(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase())
        val breakend1: LinxBreakend = TestLinxFactory.breakendBuilder().gene(gene).type(LinxBreakendType.DEL).build()
        Assert.assertEquals(0, disruptionExtractor.extractDisruptions(withBreakend(breakend1), Sets.newHashSet(gene)).size.toLong())
        val breakend2: LinxBreakend = TestLinxFactory.breakendBuilder().gene(gene).type(LinxBreakendType.DUP).build()
        Assert.assertEquals(1, disruptionExtractor.extractDisruptions(withBreakend(breakend2), Sets.newHashSet(gene)).size.toLong())
        val breakend3: LinxBreakend = TestLinxFactory.breakendBuilder().gene("other").type(LinxBreakendType.DEL).build()
        Assert.assertEquals(1, disruptionExtractor.extractDisruptions(withBreakend(breakend3), Sets.newHashSet(gene)).size.toLong())
    }

    @Test
    fun canDetermineAllDisruptionTypes() {
        for (breakendType in LinxBreakendType.values()) {
            Assert.assertNotNull(DisruptionExtractor.Companion.determineDisruptionType(breakendType))
        }
    }

    @Test
    fun canDetermineAllRegionTypes() {
        for (regionType in TranscriptRegionType.values()) {
            if (regionType != TranscriptRegionType.UNKNOWN) {
                Assert.assertNotNull(DisruptionExtractor.Companion.determineRegionType(regionType))
            }
        }
    }

    @Test
    fun canDetermineAllCodingTypes() {
        for (codingType in TranscriptCodingType.values()) {
            if (codingType != TranscriptCodingType.UNKNOWN) {
                Assert.assertNotNull(DisruptionExtractor.Companion.determineCodingContext(codingType))
            }
        }
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private fun withBreakend(breakend: LinxBreakend): LinxRecord {
            return ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addAllSomaticStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(breakend.svId()).build())
                .addAllSomaticBreakends(breakend)
                .build()
        }
    }
}