package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        PatientRecord record = TestDataFactory.createProperTestPatientRecord();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(record);
    }
}