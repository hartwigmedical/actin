package com.hartwig.actin.clinical.feed.intolerance;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;

import org.junit.Test;

public class IntoleranceFileReaderTest {

    private static final String TEST_INTOLERANCE_TSV = Resources.getResource("clinical/intolerance.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        List<IntoleranceEntry> entries = new IntoleranceFileReader().read(TEST_INTOLERANCE_TSV);
        assertEquals(1, entries.size());

        IntoleranceEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals(LocalDate.of(2014, 4, 21), entry.assertedDate());
        assertEquals("medication", entry.category());
        assertEquals("419511003", entry.categoryAllergyCategoryCode());
        assertEquals("Propensity to adverse reactions to drug", entry.categoryAllergyCategoryDisplay());
        assertEquals("active", entry.clinicalStatus());
        assertEquals("SIMVASTATINE", entry.codeText());
        assertEquals("low", entry.criticality());
    }
}