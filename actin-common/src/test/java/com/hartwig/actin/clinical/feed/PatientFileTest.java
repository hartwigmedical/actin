package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;

import org.junit.Test;

public class PatientFileTest {

    private static final String TEST_PATIENT_TSV = Resources.getResource("clinical/patient.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        List<PatientEntry> entries = PatientFile.read(TEST_PATIENT_TSV);
        assertEquals(1, entries.size());
    }
}