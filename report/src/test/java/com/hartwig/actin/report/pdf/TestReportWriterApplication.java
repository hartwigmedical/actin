package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.algo.util.TreatmentMatchPrinter;
import com.hartwig.actin.clinical.util.ClinicalPrinter;
import com.hartwig.actin.molecular.util.MolecularPrinter;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.datamodel.TestReportFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestReportWriterApplication {

    private static final Logger LOGGER = LogManager.getLogger(TestReportWriterApplication.class);

    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";

    public static void main(String[] args) throws IOException {
        Report report = TestReportFactory.createProperTestReport();

        LOGGER.info("Printing clinical record");
        ClinicalPrinter.printRecord(report.clinical());

        LOGGER.info("Printing molecular record");
        MolecularPrinter.printRecord(report.molecular());

        LOGGER.info("Printing treatment match results");
        TreatmentMatchPrinter.printMatch(report.treatmentMatch());

        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(OUTPUT_DIRECTORY);

        writer.write(report);
    }
}
