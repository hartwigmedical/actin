package com.hartwig.actin.algo;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.PatientRecordFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.doid.datamodel.DoidEntry;
import com.hartwig.actin.algo.doid.serialization.DoidJson;
import com.hartwig.actin.algo.serialization.TreatmentMatchJson;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.clinical.util.ClinicalPrinter;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;
import com.hartwig.actin.molecular.util.MolecularPrinter;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.serialization.TrialJson;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TreatmentMatcherApplication {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentMatcherApplication.class);

    private static final String APPLICATION = "ACTIN Treatment Matcher";
    private static final String VERSION = TreatmentMatcherApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        Options options = TreatmentMatcherConfig.createOptions();

        TreatmentMatcherConfig config = null;
        try {
            config = TreatmentMatcherConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new TreatmentMatcherApplication(config).run();
    }

    @NotNull
    private final TreatmentMatcherConfig config;

    private TreatmentMatcherApplication(@NotNull final TreatmentMatcherConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Loading clinical record from {}", config.clinicalJson());
        ClinicalRecord clinical = ClinicalRecordJson.read(config.clinicalJson());
        ClinicalPrinter.printRecord(clinical);

        LOGGER.info("Loading molecular record from {}", config.molecularJson());
        MolecularRecord molecular = MolecularRecordJson.read(config.molecularJson());
        MolecularPrinter.printRecord(molecular);

        PatientRecord patient = PatientRecordFactory.fromInputs(clinical, molecular);

        LOGGER.info("Loading trials from {}", config.treatmentDatabaseDirectory());
        List<Trial> trials = TrialJson.readFromDir(config.treatmentDatabaseDirectory());
        LOGGER.info(" Loaded {} trials", trials.size());

        LOGGER.info("Loading DOID tree from {}", config.doidJson());
        DoidEntry doidEntry = DoidJson.readDoidOwlEntry(config.doidJson());
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size());

        LOGGER.info(("Matching patient to available trials"));
        TreatmentMatch match = TrialMatcher.determineEligibility(patient, trials);

        TreatmentMatchJson.write(match, config.outputDirectory());

        LOGGER.info("Done!");
    }
}
