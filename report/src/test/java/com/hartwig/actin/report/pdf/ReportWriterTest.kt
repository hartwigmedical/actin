package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.report.datamodel.TestReportFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReports() throws IOException {
        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(TestReportFactory.createMinimalTestReport());
        memoryWriter.write(TestReportFactory.createProperTestReport());
        memoryWriter.write(TestReportFactory.createExhaustiveTestReport());
    }
}