package com.hartwig.actin.report;

import java.io.IOException;

import com.hartwig.actin.datamodel.ActinAnalysis;
import com.hartwig.actin.datamodel.ImmutableActinAnalysis;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.report.pdf.ReportWriter;
import com.hartwig.actin.report.pdf.ReportWriterFactory;
import com.hartwig.actin.serialization.ClinicalRecordJson;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ReportApplication {

    private static final Logger LOGGER = LogManager.getLogger(ReportApplication.class);

    public static final String VERSION = ReportApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        LOGGER.info("Running ACTIN Report v{}", VERSION);

        Options options = ReportConfig.createOptions();

        ReportConfig config = null;
        try {
            config = ReportConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp("ACTIN Report", options);
            System.exit(1);
        }

        new ReportApplication(config).run();
    }

    @NotNull
    private final ReportConfig config;

    private ReportApplication(@NotNull final ReportConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Loading clinical record from {}", config.clinicalJson());
        ClinicalRecord clinical = ClinicalRecordJson.read(config.clinicalJson());

        ActinAnalysis analysis = ImmutableActinAnalysis.builder().clinical(clinical).build();

        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory());

        writer.write(analysis);

        LOGGER.info("Done!");
    }
}
