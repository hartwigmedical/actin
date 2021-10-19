package com.hartwig.actin.report.pdf.chapters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SummaryChapterTest {

    @Test
    public void canConvertSampleToPatient() {
        assertEquals("ACTN-01-02-9999", SummaryChapter.toPatientId("ACTN01029999T"));
    }
}