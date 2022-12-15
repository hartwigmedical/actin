package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.TestPeachFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PharmacoExtractionTest {

    @Test
    public void canExtractPharmaco() {
        PeachEntry peachEntry1 = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 1").function("function 1").build();
        PeachEntry peachEntry2 = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 2").function("function 2").build();

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
    private static OrangeRecord withPeachEntries(@NotNull PeachEntry... peachEntries) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();
        return ImmutableOrangeRecord.builder()
                .from(base)
                .peach(ImmutablePeachRecord.builder().from(base.peach()).entries(Sets.newHashSet(peachEntries)).build())
                .build();
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