package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.datamodel.Printer;
import com.hartwig.actin.datamodel.TestDataFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestReportWriterApplication {

    private static final Logger LOGGER = LogManager.getLogger(TestReportWriterApplication.class);

    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    public static void main(String[] args) throws IOException {
        ActinRecord record = TestDataFactory.createTestActinRecord();

        LOGGER.info("Printing clinical record");
        Printer.printClinicalRecord(record.clinical());

        LOGGER.info("Printing molecular record");
        Printer.printMolecularRecord(record.molecular());

        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(OUTPUT_DIRECTORY);

        writer.write(record);
    }
}
