package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.TestLilacFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ImmunologyExtractionTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractImmunology() {
        LilacHlaAllele allele1 = TestLilacFactory.builder()
                .allele("allele 1")
                .tumorCopyNumber(1.2)
                .somaticMissense(1)
                .somaticInframeIndel(1)
                .somaticSplice(1)
                .somaticNonsenseOrFrameshift(0)
                .build();

        LilacHlaAllele allele2 = TestLilacFactory.builder()
                .allele("allele 2")
                .tumorCopyNumber(1.3)
                .somaticMissense(0)
                .somaticInframeIndel(0)
                .somaticSplice(0)
                .somaticNonsenseOrFrameshift(0)
                .build();

        OrangeRecord orange = withLilacData(ImmunologyExtraction.LILAC_QC_PASS, allele1, allele2);
        MolecularImmunology immunology = ImmunologyExtraction.extract(orange);

        assertTrue(immunology.isReliable());

        assertEquals(2, immunology.hlaAlleles().size());
        HlaAllele hlaAllele1 = findByName(immunology.hlaAlleles(), "allele 1");

        assertEquals(1.2, hlaAllele1.tumorCopyNumber(), EPSILON);
        assertTrue(hlaAllele1.hasSomaticMutations());

        HlaAllele hlaAllele2 = findByName(immunology.hlaAlleles(), "allele 2");

        assertEquals(1.3, hlaAllele2.tumorCopyNumber(), EPSILON);
        assertFalse(hlaAllele2.hasSomaticMutations());
    }

    @NotNull
    private static HlaAllele findByName(@NotNull Set<HlaAllele> hlaAlleles, @NotNull String nameToFind) {
        for (HlaAllele hlaAllele : hlaAlleles) {
            if (hlaAllele.name().equals(nameToFind)) {
                return hlaAllele;
            }
        }

        throw new IllegalStateException("Could not find hla allele with name: " + nameToFind);
    }

    @NotNull
    private static OrangeRecord withLilacData(@NotNull String lilacQc, @NotNull LilacHlaAllele... alleles) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();
        return ImmutableOrangeRecord.builder()
                .from(base)
                .lilac(ImmutableLilacRecord.builder().from(base.lilac()).qc(lilacQc).addAlleles(alleles).build())
                .build();
    }
}