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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberExtractorTest {

    @Test
    fun `Should extract copy numbers`() {
        val driver1: PurpleDriver = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).build()
        val gainLoss1: PurpleGainLoss = TestPurpleFactory.gainLossBuilder()
            .gene("gene 1")
            .minCopies(0.0)
            .maxCopies(1.0)
            .interpretation(CopyNumberInterpretation.PARTIAL_LOSS)
            .build()
        val gainLoss2: PurpleGainLoss = TestPurpleFactory.gainLossBuilder()
            .gene("gene 2")
            .minCopies(20.0)
            .maxCopies(21.0)
            .interpretation(CopyNumberInterpretation.FULL_GAIN)
            .build()
        val gainLoss3: PurpleGainLoss = TestPurpleFactory.gainLossBuilder()
            .gene("gene 3")
            .minCopies(20.0)
            .maxCopies(20.0)
            .interpretation(CopyNumberInterpretation.FULL_GAIN)
            .build()
        val gainLoss4: PurpleGainLoss = TestPurpleFactory.gainLossBuilder()
            .gene("gene 4")
            .minCopies(19.6)
            .maxCopies(20.4)
            .interpretation(CopyNumberInterpretation.FULL_GAIN)
            .build()
        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver1)
            .addAllSomaticGainsLosses(gainLoss1, gainLoss2, gainLoss3, gainLoss4)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(gainLoss1.gene(), gainLoss2.gene(), gainLoss4.gene())
        val copyNumberExtractor = CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())

        val copyNumbers = copyNumberExtractor.extract(purple)
        assertThat(copyNumbers).hasSize(3)

        val gene1 = findByGene(copyNumbers, "gene 1")
        assertThat(gene1.isReportable).isTrue
        assertThat(gene1.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(gene1.type).isEqualTo(CopyNumberType.LOSS)
        assertThat(gene1.minCopies).isEqualTo(0)
        assertThat(gene1.maxCopies).isEqualTo(1)

        val gene2 = findByGene(copyNumbers, "gene 2")
        assertThat(gene2.isReportable).isFalse
        assertThat(gene2.driverLikelihood).isNull()
        assertThat(gene2.type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(gene2.minCopies).isEqualTo(20)
        assertThat(gene2.maxCopies).isEqualTo(21)

        val gene4 = findByGene(copyNumbers, "gene 4")
        assertThat(gene4.minCopies).isEqualTo(20)
        assertThat(gene4.maxCopies).isEqualTo(20)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedCopyNumber() {
        val driver: PurpleDriver = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).build()
        val gainLoss: PurpleGainLoss =
            TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build()
        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver)
            .addAllSomaticGainsLosses(gainLoss)
            .build()

        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val copyNumberExtractor = CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        copyNumberExtractor.extract(purple)
    }

    @Test
    fun `Should determine type for all interpretations`() {
        for (interpretation in CopyNumberInterpretation.values()) {
            assertThat(CopyNumberExtractor.determineType(interpretation)).isNotNull()
        }
    }

    companion object {
        private fun findByGene(copyNumbers: Iterable<CopyNumber>, geneToFind: String): CopyNumber {
            return copyNumbers.find { it.gene == geneToFind }
                ?: throw IllegalStateException("Could not find copy number for gene: $geneToFind")
        }
    }
}