package com.hartwig.actin.report.pdf;

import java.io.File;
import java.io.IOException;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.serialization.TreatmentMatchJson;
import com.hartwig.actin.algo.util.TreatmentMatchPrinter;
import com.hartwig.actin.clinical.util.ClinicalPrinter;
import com.hartwig.actin.molecular.util.MolecularPrinter;
import com.hartwig.actin.report.datamodel.ImmutableReport;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.datamodel.TestReportFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TestReportWriterApplication {

    private static final Logger LOGGER = LogManager.getLogger(TestReportWriterApplication.class);

    private static final String WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp";
    private static final String OPTIONAL_TREATMENT_MATCH_JSON = WORK_DIRECTORY + File.separator + "sample.treatment_match.json";

    public static void main(String[] args) throws IOException {
        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(WORK_DIRECTORY);

        Report report = createTestReport();

        writer.write(report);
    }

    @NotNull
    private static Report createTestReport() throws IOException {
        Report report = TestReportFactory.createProperTestReport();

        LOGGER.info("Printing clinical record");
        ClinicalPrinter.printRecord(report.clinical());

        LOGGER.info("Printing molecular record");
        MolecularPrinter.printRecord(report.molecular());

        Report updated;
        if (new File(OPTIONAL_TREATMENT_MATCH_JSON).exists()) {
            LOGGER.info("Loading treatment matches from {}", OPTIONAL_TREATMENT_MATCH_JSON);
            TreatmentMatch match = TreatmentMatchJson.read(OPTIONAL_TREATMENT_MATCH_JSON);
            updated = ImmutableReport.builder().from(report).treatmentMatch(match).build();
        } else {
            updated = report;
        }

        LOGGER.info("Printing treatment match results");
        TreatmentMatchPrinter.printMatch(updated.treatmentMatch());

        return updated;
    }
}
