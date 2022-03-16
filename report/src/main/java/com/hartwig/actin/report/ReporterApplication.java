package com.hartwig.actin.report;

import java.io.IOException;

import com.hartwig.actin.PatientRecordFactory;
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

        ClinicalRecord clinical = loadClinicalRecord(config.clinicalJson());
        MolecularRecord molecular = loadMolecularRecord(config.molecularJson());
        TreatmentMatch treatments = loadTreatmentMatches(config.treatmentMatchJson());

        Report report = ReportFactory.fromInputs(PatientRecordFactory.fromInputs(clinical, molecular), treatments);
        ReportWriter writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory());

        writer.write(report);

        LOGGER.info("Done!");
    }

    @NotNull
    private static ClinicalRecord loadClinicalRecord(@NotNull String clinicalJson) throws IOException {
        LOGGER.info("Loading clinical record from {}", clinicalJson);
        return ClinicalRecordJson.read(clinicalJson);
    }

    @NotNull
    private static MolecularRecord loadMolecularRecord(@NotNull String molecularJson) throws IOException {
        LOGGER.info("Loading molecular record from {}", molecularJson);
        return  MolecularRecordJson.read(molecularJson);
    }

    @NotNull
    private static TreatmentMatch loadTreatmentMatches(@NotNull String treatmentMatchJson) throws IOException {
        LOGGER.info("Loading treatment match results from {}", treatmentMatchJson);
        return TreatmentMatchJson.read(treatmentMatchJson);
    }
}
