package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.datamodel.TestDataFactory;

public class TestReportWriterApplication {

    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    public static void main(String[] args) throws IOException {
        ActinRecord record = TestDataFactory.createTestActinRecord();

        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(OUTPUT_DIRECTORY);

        writer.write(record);
    }
}
