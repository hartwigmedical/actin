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
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CopyNumberExtractorTest {

    @Test
    public void canExtractCopyNumbers() {
        PurpleDriver driver1 = TestPurpleFactory.driverBuilder().gene("gene 1").type(PurpleDriverType.DEL).build();
        PurpleCopyNumber copyNumber1 = TestPurpleFactory.copyNumberBuilder()
                .gene("gene 1")
                .minCopies(0)
                .maxCopies(1)
                .interpretation(PurpleCopyNumberInterpretation.PARTIAL_LOSS)
                .build();

        PurpleCopyNumber copyNumber2 = TestPurpleFactory.copyNumberBuilder()
                .gene("gene 2")
                .minCopies(20)
                .maxCopies(21)
                .interpretation(PurpleCopyNumberInterpretation.FULL_GAIN)
                .build();

        PurpleCopyNumber copyNumber3 = TestPurpleFactory.copyNumberBuilder()
                .gene("gene 3")
                .minCopies(20)
                .maxCopies(20)
                .interpretation(PurpleCopyNumberInterpretation.FULL_GAIN)
                .build();

        PurpleRecord purple = ImmutablePurpleRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
                .addDrivers(driver1)
                .addCopyNumbers(copyNumber1, copyNumber2, copyNumber3)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(copyNumber1.gene(), copyNumber2.gene());
        CopyNumberExtractor copyNumberExtractor = new CopyNumberExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<CopyNumber> copyNumbers = copyNumberExtractor.extract(purple);
        assertEquals(2, copyNumbers.size());

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

    @Test
    public void canDetermineTypeForAllInterpretations() {
        for (PurpleCopyNumberInterpretation interpretation : PurpleCopyNumberInterpretation.values()) {
            assertNotNull(CopyNumberExtractor.determineType(interpretation));
        }
    }
}