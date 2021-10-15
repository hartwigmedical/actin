package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.datamodel.ActinAnalysis;
import com.hartwig.actin.datamodel.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        ActinAnalysis analysis = TestDataFactory.createTestActinAnalysis();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(analysis);
    }
}