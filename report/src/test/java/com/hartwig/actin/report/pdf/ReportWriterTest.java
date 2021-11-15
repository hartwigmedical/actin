package com.hartwig.actin.report.pdf;

import java.io.IOException;

import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.datamodel.TestReportFactory;

import org.junit.Test;

public class ReportWriterTest {

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        Report report = TestReportFactory.createProperTestReport();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(report);
    }
}