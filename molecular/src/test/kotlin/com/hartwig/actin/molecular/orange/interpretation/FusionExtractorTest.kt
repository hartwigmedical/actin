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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FusionExtractorTest {

    @Test
    fun canExtractFusions() {
        val linxFusion: LinxFusion = TestLinxFactory.fusionBuilder()
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
        val linx: LinxRecord = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addAllSomaticFusions(linxFusion)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("gene end")
        val fusionExtractor = FusionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())

        val fusions = fusionExtractor.extract(linx)
        assertEquals(1, fusions.size.toLong())

        val fusion = fusions.iterator().next()
        assertTrue(fusion.isReportable)
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood())
        assertEquals("gene start", fusion.geneStart())
        assertEquals("trans start", fusion.geneTranscriptStart())
        assertEquals(1, fusion.fusedExonUp().toLong())
        assertEquals("gene end", fusion.geneEnd())
        assertEquals("trans end", fusion.geneTranscriptEnd())
        assertEquals(4, fusion.fusedExonDown().toLong())
        assertEquals(FusionDriverType.PROMISCUOUS_5, fusion.driverType())
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedFusion() {
        val linxFusion: LinxFusion = TestLinxFactory.fusionBuilder().reported(true).geneStart("other start").geneEnd("other end").build()
        val linx: LinxRecord = ImmutableLinxRecord.builder()
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
            val fusion: LinxFusion = TestLinxFactory.fusionBuilder().reportedType(type).build()
            assertNotNull(FusionExtractor.determineDriverType(fusion))
        }
    }

    @Test
    fun canDetermineDriverLikelihoodForAllFusions() {
        val high: LinxFusion = TestLinxFactory.fusionBuilder().driverLikelihood(FusionLikelihoodType.HIGH).build()
        assertEquals(DriverLikelihood.HIGH, FusionExtractor.determineDriverLikelihood(high))

        val low: LinxFusion = TestLinxFactory.fusionBuilder().driverLikelihood(FusionLikelihoodType.LOW).build()
        assertEquals(DriverLikelihood.LOW, FusionExtractor.determineDriverLikelihood(low))

        val na: LinxFusion = TestLinxFactory.fusionBuilder().driverLikelihood(FusionLikelihoodType.NA).build()
        assertNull(FusionExtractor.determineDriverLikelihood(na))
    }
}