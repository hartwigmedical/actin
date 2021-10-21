package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.util.ClinicalPrinter;
import com.hartwig.actin.molecular.util.MolecularPrinter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestReportWriterApplication {

    private static final Logger LOGGER = LogManager.getLogger(TestReportWriterApplication.class);

    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    public static void main(String[] args) throws IOException {
        PatientRecord record = TestDataFactory.createTestPatientRecord();

        LOGGER.info("Printing clinical record");
        ClinicalPrinter.printRecord(record.clinical());

        LOGGER.info("Printing molecular record");
        MolecularPrinter.printRecord(record.molecular());

        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(OUTPUT_DIRECTORY);

        writer.write(record);
    }
}
