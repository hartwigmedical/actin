package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import org.junit.Assert
import org.junit.Test

class CopyNumberExtractorTest {
    @Test
    fun canExtractCopyNumbers() {
        val driver1: PurpleDriver? = TestPurpleFactory.driverBuilder().gene("gene 1").driver(PurpleDriverType.DEL).build()
        val gainLoss1: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder()
            .gene("gene 1")
            .minCopies(0.0)
            .maxCopies(1.0)
            .interpretation(CopyNumberInterpretation.PARTIAL_LOSS)
            .build()
        val gainLoss2: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder()
            .gene("gene 2")
            .minCopies(20.0)
            .maxCopies(21.0)
            .interpretation(CopyNumberInterpretation.FULL_GAIN)
            .build()
        val gainLoss3: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder()
            .gene("gene 3")
            .minCopies(20.0)
            .maxCopies(20.0)
            .interpretation(CopyNumberInterpretation.FULL_GAIN)
            .build()
        val gainLoss4: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder()
            .gene("gene 4")
            .minCopies(19.6)
            .maxCopies(20.4)
            .interpretation(CopyNumberInterpretation.FULL_GAIN)
            .build()
        val purple: PurpleRecord? = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver1)
            .addAllSomaticGainsLosses(gainLoss1, gainLoss2, gainLoss3, gainLoss4)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(gainLoss1.gene(), gainLoss2.gene(), gainLoss4.gene())
        val copyNumberExtractor = CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        val copyNumbers = copyNumberExtractor.extract(purple)
        Assert.assertEquals(3, copyNumbers.size.toLong())
        val gene1 = findByGene(copyNumbers, "gene 1")
        Assert.assertTrue(gene1.isReportable())
        Assert.assertEquals(DriverLikelihood.HIGH, gene1.driverLikelihood())
        Assert.assertEquals(CopyNumberType.LOSS, gene1.type())
        Assert.assertEquals(0, gene1.minCopies().toLong())
        Assert.assertEquals(1, gene1.maxCopies().toLong())
        val gene2 = findByGene(copyNumbers, "gene 2")
        Assert.assertFalse(gene2.isReportable())
        Assert.assertNull(gene2.driverLikelihood())
        Assert.assertEquals(CopyNumberType.FULL_GAIN, gene2.type())
        Assert.assertEquals(20, gene2.minCopies().toLong())
        Assert.assertEquals(21, gene2.maxCopies().toLong())
        val gene4 = findByGene(copyNumbers, "gene 4")
        Assert.assertEquals(20, gene4.minCopies().toLong())
        Assert.assertEquals(20, gene4.maxCopies().toLong())
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedCopyNumber() {
        val driver: PurpleDriver? = TestPurpleFactory.driverBuilder().gene("gene 1").driver(PurpleDriverType.DEL).build()
        val gainLoss: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build()
        val purple: PurpleRecord? = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver)
            .addAllSomaticGainsLosses(gainLoss)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val copyNumberExtractor = CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        copyNumberExtractor.extract(purple)
    }

    @Test
    fun canDetermineTypeForAllInterpretations() {
        for (interpretation in CopyNumberInterpretation.values()) {
            Assert.assertNotNull(CopyNumberExtractor.Companion.determineType(interpretation))
        }
    }

    companion object {
        private fun findByGene(copyNumbers: Iterable<CopyNumber?>, geneToFind: String): CopyNumber {
            for (copyNumber in copyNumbers) {
                if (copyNumber.gene() == geneToFind) {
                    return copyNumber
                }
            }
            throw IllegalStateException("Could not find copy number for gene: $geneToFind")
        }
    }
}