package com.hartwig.actin.report;

import java.io.IOException;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.serialization.TreatmentMatchJson;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.datamodel.ReportFactory;
import com.hartwig.actin.report.pdf.ReportWriter;
import com.hartwig.actin.report.pdf.ReportWriterFactory;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ReporterApplication {

    private static final Logger LOGGER = LogManager.getLogger(ReporterApplication.class);

    private static final String APPLICATION = "ACTIN Reporter";
    public static final String VERSION = ReporterApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = ReporterConfig.createOptions();

        ReporterConfig config = null;
        try {
            config = ReporterConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new ReporterApplication(config).run();
    }

    @NotNull
    private final ReporterConfig config;

    private ReporterApplication(@NotNull final ReporterConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading clinical record from {}", config.clinicalJson());
        ClinicalRecord clinical = ClinicalRecordJson.read(config.clinicalJson());

        LOGGER.info("Loading molecular record from {}", config.molecularJson());
        MolecularRecord molecular = MolecularRecordJson.read(config.molecularJson());

        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson());
        TreatmentMatch treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson());

        Report report = ReportFactory.fromInputs(clinical, molecular, treatmentMatch);
        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory());

        writer.write(report);

        LOGGER.info("Done!");
    }
}
