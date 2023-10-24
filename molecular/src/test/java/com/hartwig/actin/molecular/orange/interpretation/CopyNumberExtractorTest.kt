package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.hmftools.datamodel.purple.PurpleDriver;
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.hmftools.datamodel.purple.PurpleRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CopyNumberExtractorTest {

    @Test
    public void canExtractCopyNumbers() {
        PurpleDriver driver1 = TestPurpleFactory.driverBuilder().gene("gene 1").driver(PurpleDriverType.DEL).build();
        PurpleGainLoss gainLoss1 = TestPurpleFactory.gainLossBuilder()
                .gene("gene 1")
                .minCopies(0)
                .maxCopies(1)
                .interpretation(CopyNumberInterpretation.PARTIAL_LOSS)
                .build();

        PurpleGainLoss gainLoss2 = TestPurpleFactory.gainLossBuilder()
                .gene("gene 2")
                .minCopies(20)
                .maxCopies(21)
                .interpretation(CopyNumberInterpretation.FULL_GAIN)
                .build();

        PurpleGainLoss gainLoss3 = TestPurpleFactory.gainLossBuilder()
                .gene("gene 3")
                .minCopies(20)
                .maxCopies(20)
                .interpretation(CopyNumberInterpretation.FULL_GAIN)
                .build();

        PurpleGainLoss gainLoss4 = TestPurpleFactory.gainLossBuilder()
                .gene("gene 4")
                .minCopies(19.6)
                .maxCopies(20.4)
                .interpretation(CopyNumberInterpretation.FULL_GAIN)
                .build();

        PurpleRecord purple = ImmutablePurpleRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
                .addSomaticDrivers(driver1)
                .addAllSomaticGainsLosses(gainLoss1, gainLoss2, gainLoss3, gainLoss4)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(gainLoss1.gene(), gainLoss2.gene(), gainLoss4.gene());
        CopyNumberExtractor copyNumberExtractor = new CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<CopyNumber> copyNumbers = copyNumberExtractor.extract(purple);
        assertEquals(3, copyNumbers.size());

        CopyNumber gene1 = findByGene(copyNumbers, "gene 1");
        assertTrue(gene1.isReportable());
        assertEquals(DriverLikelihood.HIGH, gene1.driverLikelihood());
        assertEquals(CopyNumberType.LOSS, gene1.type());
        assertEquals(0, gene1.minCopies());
        assertEquals(1, gene1.maxCopies());

        CopyNumber gene2 = findByGene(copyNumbers, "gene 2");
        assertFalse(gene2.isReportable());
        assertNull(gene2.driverLikelihood());
        assertEquals(CopyNumberType.FULL_GAIN, gene2.type());
        assertEquals(20, gene2.minCopies());
        assertEquals(21, gene2.maxCopies());

        CopyNumber gene4 = findByGene(copyNumbers, "gene 4");
        assertEquals(20, gene4.minCopies());
        assertEquals(20, gene4.maxCopies());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenFilteringReportedCopyNumber() {
        PurpleDriver driver = TestPurpleFactory.driverBuilder().gene("gene 1").driver(PurpleDriverType.DEL).build();
        PurpleGainLoss gainLoss =
                TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.PARTIAL_LOSS).build();

        PurpleRecord purple = ImmutablePurpleRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
                .addSomaticDrivers(driver)
                .addAllSomaticGainsLosses(gainLoss)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene");
        CopyNumberExtractor copyNumberExtractor = new CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());
        copyNumberExtractor.extract(purple);
    }

    @Test
    public void canDetermineTypeForAllInterpretations() {
        for (CopyNumberInterpretation interpretation : CopyNumberInterpretation.values()) {
            assertNotNull(CopyNumberExtractor.determineType(interpretation));
        }
    }

    @NotNull
    private static CopyNumber findByGene(@NotNull Iterable<CopyNumber> copyNumbers, @NotNull String geneToFind) {
        for (CopyNumber copyNumber : copyNumbers) {
            if (copyNumber.gene().equals(geneToFind)) {
                return copyNumber;
            }
        }

        throw new IllegalStateException("Could not find copy number for gene: " + geneToFind);
    }
}