package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.peach.TestPeachFactory;
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord;
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;
import com.hartwig.hmftools.datamodel.peach.PeachGenotype;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PharmacoExtractionTest {

    @Test
    public void canExtractPharmaco() {
        PeachGenotype peachEntry1 = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 1").function("function 1").build();
        PeachGenotype peachEntry2 = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 2").function("function 2").build();

        OrangeRecord orange = withPeachEntries(peachEntry1, peachEntry2);
        Set<PharmacoEntry> entries = PharmacoExtraction.extract(orange);

        assertEquals(1, entries.size());

        PharmacoEntry entry = entries.iterator().next();
        assertEquals("gene 1", entry.gene());
        assertEquals(2, entry.haplotypes().size());

        Haplotype haplotype1 = findByName(entry.haplotypes(), "haplotype 1");
        assertEquals("function 1", haplotype1.function());

        Haplotype haplotype2 = findByName(entry.haplotypes(), "haplotype 2");
        assertEquals("function 2", haplotype2.function());
    }

    @NotNull
    private static OrangeRecord withPeachEntries(@NotNull PeachGenotype... peachEntries) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();
        return ImmutableOrangeRecord.builder().from(base).addPeach(peachEntries).build();
    }

    @NotNull
    private static Haplotype findByName(@NotNull Set<Haplotype> haplotypes, @NotNull String nameToFind) {
        for (Haplotype haplotype : haplotypes) {
            if (haplotype.name().equals(nameToFind)) {
                return haplotype;
            }
        }

        throw new IllegalStateException("Could not find haplotype with name: " + nameToFind);
    }
}