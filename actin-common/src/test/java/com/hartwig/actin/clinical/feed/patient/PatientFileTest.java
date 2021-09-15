package com.hartwig.actin.clinical.feed.patient;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class PatientFileTest {

    private static final String TEST_PATIENT_TSV = Resources.getResource("clinical/patient.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        assertEquals(1, PatientFile.read(TEST_PATIENT_TSV).size());
    }
}