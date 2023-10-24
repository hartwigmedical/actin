package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import com.hartwig.hmftools.datamodel.linx.LinxRecord
import org.junit.Assert
import org.junit.Test

class FusionExtractorTest {
    @Test
    fun canExtractFusions() {
        val linxFusion: LinxFusion? = TestLinxFactory.fusionBuilder()
            .reported(true)
            .reportedType(LinxFusionType.PROMISCUOUS_5)
            .geneStart("gene start")
            .geneTranscriptStart("trans start")
            .fusedExonUp(1)
            .geneEnd("gene end")
            .geneTranscriptEnd("trans end")
            .fusedExonDown(4)
            .likelihood(FusionLikelihoodType.HIGH)
            .build()
        val linx: LinxRecord? = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticFusions(linxFusion)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("gene end")
        val fusionExtractor = FusionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        val fusions = fusionExtractor.extract(linx)
        Assert.assertEquals(1, fusions.size.toLong())
        val fusion = fusions.iterator().next()
        Assert.assertTrue(fusion.isReportable())
        Assert.assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood())
        Assert.assertEquals("gene start", fusion.geneStart())
        Assert.assertEquals("trans start", fusion.geneTranscriptStart())
        Assert.assertEquals(1, fusion.fusedExonUp().toLong())
        Assert.assertEquals("gene end", fusion.geneEnd())
        Assert.assertEquals("trans end", fusion.geneTranscriptEnd())
        Assert.assertEquals(4, fusion.fusedExonDown().toLong())
        Assert.assertEquals(FusionDriverType.PROMISCUOUS_5, fusion.driverType())
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedFusion() {
        val linxFusion: LinxFusion? = TestLinxFactory.fusionBuilder().reported(true).geneStart("other start").geneEnd("other end").build()
        val linx: LinxRecord? = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticFusions(linxFusion)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val fusionExtractor = FusionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        fusionExtractor.extract(linx)
    }

    @Test
    fun canDetermineDriverTypeForAllFusions() {
        for (type in LinxFusionType.values()) {
            val fusion: LinxFusion? = TestLinxFactory.fusionBuilder().reportedType(type).build()
            Assert.assertNotNull(FusionExtractor.Companion.determineDriverType(fusion))
        }
    }

    @Test
    fun canDetermineDriverLikelihoodForAllFusions() {
        val high: LinxFusion? = TestLinxFactory.fusionBuilder().likelihood(FusionLikelihoodType.HIGH).build()
        Assert.assertEquals(DriverLikelihood.HIGH, FusionExtractor.Companion.determineDriverLikelihood(high))
        val low: LinxFusion? = TestLinxFactory.fusionBuilder().likelihood(FusionLikelihoodType.LOW).build()
        Assert.assertEquals(DriverLikelihood.LOW, FusionExtractor.Companion.determineDriverLikelihood(low))
        val na: LinxFusion? = TestLinxFactory.fusionBuilder().likelihood(FusionLikelihoodType.NA).build()
        Assert.assertNull(FusionExtractor.Companion.determineDriverLikelihood(na))
    }
}