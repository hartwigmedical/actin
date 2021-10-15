package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.datamodel.ActinAnalysis;
import com.hartwig.actin.datamodel.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    private static final boolean RUN_PDF_WRITING_TEST = false;
    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        ActinAnalysis analysis = TestDataFactory.createTestActinAnalysis();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(analysis);
    }

    @Test
    public void generateProductionReport() throws IOException {
        if (RUN_PDF_WRITING_TEST) {
            ActinAnalysis analysis = TestDataFactory.createTestActinAnalysis();

            ReportWriter writer = ReportWriterFactory.createProductionReportWriter(OUTPUT_DIRECTORY);

            writer.write(analysis);
        }
    }
}