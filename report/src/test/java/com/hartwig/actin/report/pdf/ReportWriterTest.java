package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.datamodel.TestDataFactory;

import org.junit.Test;

public class ReportWriterTest {

    private static final boolean RUN_PDF_WRITING_TEST = false;
    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    @Test
    public void canGenerateInMemoryReport() throws IOException {
        ActinRecord record = TestDataFactory.createTestActinRecord();

        ReportWriter memoryWriter = ReportWriterFactory.createInMemoryReportWriter();

        memoryWriter.write(record);
    }

    @Test
    public void generateProductionReport() throws IOException {
        if (RUN_PDF_WRITING_TEST) {
            ActinRecord record = TestDataFactory.createTestActinRecord();

            ReportWriter writer = ReportWriterFactory.createProductionReportWriter(OUTPUT_DIRECTORY);

            writer.write(record);
        }
    }
}