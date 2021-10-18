package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.datamodel.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        ActinRecord record = TestDataFactory.createTestActinRecord();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(record);
    }
}