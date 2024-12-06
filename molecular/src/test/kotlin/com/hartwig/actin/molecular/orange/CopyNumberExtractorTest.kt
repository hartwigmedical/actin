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
        val driverGene2NonCanonical = TestPurpleFactory.driverBuilder().gene("gene 2").type(PurpleDriverType.AMP).isCanonical(false).build()
        val driverGene3Canonical = TestPurpleFactory.driverBuilder().gene("gene 3").type(PurpleDriverType.AMP).isCanonical(true).build()
        val driverGene3NonCanonical = TestPurpleFactory.driverBuilder().gene("gene 3").type(PurpleDriverType.AMP).isCanonical(false).build()
        val driverGene4Canonical = TestPurpleFactory.driverBuilder().gene("gene 4").type(PurpleDriverType.AMP).isCanonical(true).build()

        val loss1Canonical = TestPurpleFactory.gainLossBuilder().gene("gene 1").minCopies(0.0).maxCopies(1.0).isCanonical(true)
            .interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build()
        val loss1NonCanonical = TestPurpleFactory.gainLossBuilder().gene("gene 1").minCopies(0.0).maxCopies(1.0).isCanonical(false)
            .interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build()
        val gain2Canonical = TestPurpleFactory.gainLossBuilder().gene("gene 2").minCopies(20.0).maxCopies(21.0).isCanonical(true)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val gain2NonCanonical = TestPurpleFactory.gainLossBuilder().gene("gene 2").minCopies(20.0).maxCopies(21.0).isCanonical(false)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val gain3Canonical = TestPurpleFactory.gainLossBuilder().gene("gene 3").minCopies(20.0).maxCopies(20.0).isCanonical(true)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val gain3NonCanonical = TestPurpleFactory.gainLossBuilder().gene("gene 3").minCopies(20.0).maxCopies(20.0).isCanonical(false)
            .interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val gain4Canonical = TestPurpleFactory.gainLossBuilder().gene("gene 4").minCopies(19.6).maxCopies(20.4).isCanonical(true)
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
        val geneCopyNumber6 =
            TestPurpleFactory.geneCopyNumberBuilder().gene("gene 6").minCopyNumber(14.0).maxCopyNumber(15.0).minMinorAlleleCopyNumber(2.0)
                .build()

        val purple = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driverGene3Canonical, driverGene3NonCanonical, driverGene2NonCanonical, driverGene4Canonical)
            .addAllSomaticGainsLosses(
                loss1Canonical,
                loss1NonCanonical,
                gain2NonCanonical,
                gain2Canonical,
                gain3Canonical,
                gain3NonCanonical,
                gain4Canonical
            )
            .addAllSomaticGeneCopyNumbers(
                geneCopyNumber1,
                geneCopyNumber2,
                geneCopyNumber3,
                geneCopyNumber4,
                geneCopyNumber5,
                geneCopyNumber6
            )
            .build()

        val geneFilter =
            TestGeneFilterFactory.createValidForGenes(
                loss1Canonical.gene(),
                gain2Canonical.gene(),
                gain3Canonical.gene(),
                gain4Canonical.gene(),
                geneCopyNumber5.gene()
            )
        val copyNumberExtractor = CopyNumberExtractor(geneFilter)
        val copyNumbers = copyNumberExtractor.extract(purple)
        assertThat(copyNumbers).hasSize(5)

        val gene1 = findByGene(copyNumbers, "gene 1")
        assertThat(gene1.isReportable).isFalse
        assertThat(gene1.driverLikelihood).isNull()
        assertThat(gene1.canonicalImpact.type).isEqualTo(CopyNumberType.NONE)
        assertThat(gene1.canonicalImpact.minCopies).isEqualTo(1)
        assertThat(gene1.canonicalImpact.maxCopies).isEqualTo(1)
        assertThat(gene1.otherImpacts).isEmpty()

        val gene2 = findByGene(copyNumbers, "gene 2")
        assertThat(gene2.isReportable).isTrue
        assertThat(gene2.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(gene2.canonicalImpact.type).isEqualTo(CopyNumberType.NONE)
        assertThat(gene2.canonicalImpact.minCopies).isEqualTo(4)
        assertThat(gene2.canonicalImpact.maxCopies).isEqualTo(5)
        assertThat(gene2.otherImpacts.first().type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(gene2.otherImpacts.first().minCopies).isEqualTo(20)
        assertThat(gene2.otherImpacts.first().maxCopies).isEqualTo(21)

        val gene3 = findByGene(copyNumbers, "gene 3")
        assertThat(gene3.isReportable).isTrue
        assertThat(gene3.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(gene3.canonicalImpact.type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(gene3.canonicalImpact.minCopies).isEqualTo(20)
        assertThat(gene3.canonicalImpact.maxCopies).isEqualTo(20)
        assertThat(gene3.otherImpacts.first().type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(gene3.otherImpacts.first().minCopies).isEqualTo(20)
        assertThat(gene3.otherImpacts.first().maxCopies).isEqualTo(20)

        val gene4 = findByGene(copyNumbers, "gene 4")
        assertThat(gene4.isReportable).isTrue
        assertThat(gene4.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(gene4.canonicalImpact.type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(gene4.canonicalImpact.minCopies).isEqualTo(20)
        assertThat(gene4.canonicalImpact.maxCopies).isEqualTo(20)
        assertThat(gene4.otherImpacts).isEmpty()

        val gene5 = findByGene(copyNumbers, "gene 5")
        assertThat(gene5.isReportable).isFalse
        assertThat(gene5.driverLikelihood).isNull()
        assertThat(gene5.canonicalImpact.type).isEqualTo(CopyNumberType.NONE)
        assertThat(gene5.canonicalImpact.minCopies).isEqualTo(14)
        assertThat(gene5.canonicalImpact.maxCopies).isEqualTo(15)
        assertThat(gene5.otherImpacts).isEmpty()
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

    private fun findByGene(copyNumbers: Iterable<CopyNumber>, geneToFind: String): CopyNumber {
        return copyNumbers.find { it.gene == geneToFind }
            ?: throw IllegalStateException("Could not find copy number for gene: $geneToFind")
    }
}