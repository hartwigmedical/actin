package com.hartwig.actin.clinical.feed.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.Sex;

import org.junit.Test;

public class PatientFileTest {

    private static final String TEST_PATIENT_TSV = Resources.getResource("clinical/patient.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        List<PatientEntry> entries = PatientFile.read(TEST_PATIENT_TSV);
        assertEquals(1, entries.size());

        PatientEntry entry = entries.get(0);

        assertEquals("CODE", entry.id());
        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals(1953, entry.birthYear());
        assertEquals(Sex.MALE, entry.sex());
        assertEquals(LocalDate.of(2020, 7, 13), entry.periodStart());
        assertNull(entry.periodEnd());
    }
}