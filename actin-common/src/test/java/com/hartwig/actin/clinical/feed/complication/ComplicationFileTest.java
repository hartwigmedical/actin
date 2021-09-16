package com.hartwig.actin.clinical.feed.complication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;

import org.junit.Test;

public class ComplicationFileTest {

    private static final String TEST_COMPLICATION_TSV = Resources.getResource("clinical/complication.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        List<ComplicationEntry> entries = ComplicationFile.read(TEST_COMPLICATION_TSV);
        assertEquals(1, entries.size());

        ComplicationEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals("EPD", entry.identifierSystem());
        assertEquals("D", entry.categoryCodeOriginal());
        assertEquals("Diagnosis", entry.categoryDisplay());
        assertEquals("Diagnose", entry.categoryDisplayOriginal());
        assertEquals("active", entry.clinicalStatus());
        assertEquals("0000001543", entry.codeCodeOriginal());
        assertEquals("CUP", entry.codeDisplayOriginal());
        assertEquals("C80.0", entry.codeCode());
        assertEquals("CUP", entry.codeDisplay());
        assertNull(entry.onsetPeriodEnd());
        assertEquals(LocalDate.of(2019, 8, 13), entry.onsetPeriodStart());
        assertTrue(entry.severityCode().isEmpty());
        assertTrue(entry.severityDisplay().isEmpty());
        assertTrue(entry.severityDisplayNl().isEmpty());
        assertEquals("ONC", entry.specialtyCodeOriginal());
        assertEquals("Oncologie", entry.specialtyDisplayOriginal());
        assertTrue(entry.verificationStatusCode().isEmpty());
    }
}