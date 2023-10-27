package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.linx.LinxRecord
import org.junit.Assert
import org.junit.Test

class HomozygousDisruptionExtractorTest {
    @Test
    fun canExtractHomozygousDisruptions() {
        val linxHomDisruption: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
        val linx: LinxRecord = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addSomaticHomozygousDisruptions(linxHomDisruption)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(linxHomDisruption.gene())
        val homDisruptionExtractor = HomozygousDisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        val homDisruptions = homDisruptionExtractor.extractHomozygousDisruptions(linx)
        Assert.assertEquals(1, homDisruptions.size.toLong())
        val homDisruption = homDisruptions.iterator().next()
        Assert.assertTrue(homDisruption.isReportable())
        Assert.assertEquals(DriverLikelihood.HIGH, homDisruption.driverLikelihood())
        Assert.assertEquals("gene 1", homDisruption.gene())
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedHomozygousDisruption() {
        val linxHomDisruption: LinxHomozygousDisruption? = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
        val linx: LinxRecord = ImmutableLinxRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
            .addSomaticHomozygousDisruptions(linxHomDisruption)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("other gene")
        val homDisruptionExtractor = HomozygousDisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        homDisruptionExtractor.extractHomozygousDisruptions(linx)
    }
}