package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.common.ActinRecord;
import com.hartwig.actin.common.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        ActinRecord record = TestDataFactory.createTestActinRecord();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(record);
    }
}