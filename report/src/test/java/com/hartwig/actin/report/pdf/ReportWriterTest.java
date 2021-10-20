package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.ActinRecord;
import com.hartwig.actin.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        ActinRecord record = TestDataFactory.createTestActinRecord();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(record);
    }
}