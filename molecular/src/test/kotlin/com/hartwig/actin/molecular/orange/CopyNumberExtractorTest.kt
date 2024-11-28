package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberExtractorTest {

    private val extractor = CopyNumberExtractor(TestGeneFilterFactory.createAlwaysValid())

    @Test
    fun `Should extract copy numbers`() {
        val driver1 = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).isCanonical(true).build()

        val gainLoss1 = TestPurpleFactory.gainLossBuilder().gene("gene 1").minCopies(0.0).maxCopies(1.0)
            .interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build()
        val gainLoss2 = TestPurpleFactory.gainLossBuilder().gene("gene 2").minCopies(20.0).maxCopies(21.0)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val gainLoss3 = TestPurpleFactory.gainLossBuilder().gene("gene 3").minCopies(20.0).maxCopies(20.0)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val gainLoss4 = TestPurpleFactory.gainLossBuilder().gene("gene 4").minCopies(19.6).maxCopies(20.4)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()

        val geneCopyNumber1 =
            TestPurpleFactory.geneCopyNumberBuilder().gene("gene 1").minCopyNumber(1.1).maxCopyNumber(1.1).minMinorAlleleCopyNumber(0.0)
                .build()
        val geneCopyNumber2 =
            TestPurpleFactory.geneCopyNumberBuilder().gene("gene 2").minCopyNumber(4.2).maxCopyNumber(5.2).minMinorAlleleCopyNumber(1.8)
                .build()
        val geneCopyNumber3 =
            TestPurpleFactory.geneCopyNumberBuilder().gene("gene 3").minCopyNumber(20.0).maxCopyNumber(20.0).minMinorAlleleCopyNumber(2.0)
                .build()
        val geneCopyNumber4 =
            TestPurpleFactory.geneCopyNumberBuilder().gene("gene 4").minCopyNumber(1.0).maxCopyNumber(2.0).minMinorAlleleCopyNumber(0.3)
                .build()
        val geneCopyNumber5 =
            TestPurpleFactory.geneCopyNumberBuilder().gene("gene 5").minCopyNumber(14.0).maxCopyNumber(15.0).minMinorAlleleCopyNumber(2.0)
                .build()

        val purple = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver1)
            .addAllSomaticGainsLosses(gainLoss1, gainLoss2, gainLoss3, gainLoss4)
            .addAllSomaticGeneCopyNumbers(geneCopyNumber1, geneCopyNumber2, geneCopyNumber3, geneCopyNumber4, geneCopyNumber5)
            .build()

        val geneFilter =
            TestGeneFilterFactory.createValidForGenes(gainLoss1.gene(), gainLoss2.gene(), gainLoss4.gene(), geneCopyNumber5.gene())
        val copyNumberExtractor = CopyNumberExtractor(geneFilter)
        val copyNumbers = copyNumberExtractor.extract(purple)
        assertThat(copyNumbers).hasSize(4)

        val gene1 = findByGene(copyNumbers, "gene 1")
        assertThat(gene1.isReportable).isTrue
        assertThat(gene1.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(gene1.type).isEqualTo(CopyNumberType.LOSS)
        assertThat(gene1.minCopies).isEqualTo(0)
        assertThat(gene1.maxCopies).isEqualTo(1)

        val gene2 = findByGene(copyNumbers, "gene 2")
        assertThat(gene2.isReportable).isFalse
        assertThat(gene2.driverLikelihood).isNull()
        assertThat(gene2.type).isEqualTo(CopyNumberType.NONE)
        assertThat(gene2.minCopies).isEqualTo(4)
        assertThat(gene2.maxCopies).isEqualTo(5)

        val gene4 = findByGene(copyNumbers, "gene 4")
        assertThat(gene4.isReportable).isFalse
        assertThat(gene4.driverLikelihood).isNull()
        assertThat(gene4.type).isEqualTo(CopyNumberType.NONE)
        assertThat(gene4.minCopies).isEqualTo(1)
        assertThat(gene4.maxCopies).isEqualTo(2)

        val gene5 = findByGene(copyNumbers, "gene 5")
        assertThat(gene5.isReportable).isFalse
        assertThat(gene5.driverLikelihood).isNull()
        assertThat(gene5.type).isEqualTo(CopyNumberType.NONE)
        assertThat(gene5.minCopies).isEqualTo(14)
        assertThat(gene5.maxCopies).isEqualTo(15)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when filtering reported copy number`() {
        val driver = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).isCanonical(true).build()
        val gainLoss = TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build()
        val geneCopyNumber = TestPurpleFactory.geneCopyNumberBuilder().gene("gene 1").build()

        val purple = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver)
            .addAllSomaticGainsLosses(gainLoss)
            .addAllSomaticGeneCopyNumbers(geneCopyNumber)
            .build()

        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val copyNumberExtractor = CopyNumberExtractor(geneFilter)
        copyNumberExtractor.extract(purple)
    }

    @Test
    fun `Should determine type for all interpretations`() {
        for (interpretation in CopyNumberInterpretation.values()) {
            assertThat(extractor.determineType(interpretation)).isNotNull()
        }
    }

    @Test
    fun `Should favor canonical over non-canonical drivers`() {
        val canonicalDriver = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).isCanonical(true).build()
        val nonCanonicalDriver = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).isCanonical(false).build()

        val gainLossGene1Canonical =
            TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.PARTIAL_LOSS).isCanonical(true)
                .minCopies(4.0).build()
        val gainLossGene1NotCanonical =
            TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.PARTIAL_LOSS).isCanonical(false)
                .minCopies(5.0).build()
        val gainLossGene2Canonical =
            TestPurpleFactory.gainLossBuilder().gene("gene 2").interpretation(CopyNumberInterpretation.FULL_GAIN).isCanonical(true)
                .minCopies(10.0).build()
        val geneCopyNumber = TestPurpleFactory.geneCopyNumberBuilder().gene("gene 1").build()

        val purple = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(canonicalDriver, nonCanonicalDriver)
            .addAllSomaticGainsLosses(gainLossGene1NotCanonical, gainLossGene2Canonical, gainLossGene1Canonical)
            .addAllSomaticGeneCopyNumbers(geneCopyNumber)
            .build()

        val copyNumbers = extractor.extract(purple)

        assertThat(copyNumbers).hasSize(1)
        assertThat(copyNumbers.first().minCopies).isEqualTo(4)
    }

    @Test
    fun `Should fall-back to non-canonical if there is no canonical driver`() {
        val gene = "gene 1"
        val nonCanonicalDriver = TestPurpleFactory.driverBuilder().gene(gene).type(PurpleDriverType.AMP).isCanonical(false).build()

        val nonCanonicalGain =
            TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(CopyNumberInterpretation.FULL_GAIN).isCanonical(false)
                .minCopies(12.0).build()
        val geneCopyNumber = TestPurpleFactory.geneCopyNumberBuilder().gene(gene).build()

        val purple = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(nonCanonicalDriver)
            .addAllSomaticGainsLosses(nonCanonicalGain)
            .addAllSomaticGeneCopyNumbers(geneCopyNumber)
            .build()

        val copyNumbers = extractor.extract(purple)

        assertThat(copyNumbers).hasSize(1)
        assertThat(copyNumbers.first().minCopies).isEqualTo(12)
    }

    private fun findByGene(copyNumbers: Iterable<CopyNumber>, geneToFind: String): CopyNumber {
        return copyNumbers.find { it.gene == geneToFind }
            ?: throw IllegalStateException("Could not find copy number for gene: $geneToFind")
    }
}