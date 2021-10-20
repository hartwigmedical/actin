package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PatientIdExtractorTest {

    @Test
    public void canExtractPatientIdFromSampleId() {
        assertEquals("ACTN-01-02-9999", PatientIdExtractor.toPatientId("ACTN01029999T"));
        assertEquals("NotValid", PatientIdExtractor.toPatientId("NotValid"));
    }
}