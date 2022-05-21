package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;

import org.junit.Test;

public class PharmacoExtractionTest {

    @Test
    public void canExtractPharmaco() {
        Set<PharmacoEntry> entries = PharmacoExtraction.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(1, entries.size());

        PharmacoEntry entry = entries.iterator().next();
        assertEquals("DPYD", entry.gene());
        assertEquals(1, entry.haplotypes().size());

        Haplotype haplotype = entry.haplotypes().iterator().next();
        assertEquals("1* HOM", haplotype.name());
        assertEquals("Normal function", haplotype.function());
    }
}