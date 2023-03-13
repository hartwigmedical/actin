package com.hartwig.actin.algo;

import java.io.IOException;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.PatientRecordFactory;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.clinical.util.ClinicalPrinter;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.DoidModelFactory;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.serialization.DoidJson;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;
import com.hartwig.actin.molecular.util.MolecularPrinter;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class StandardOfCareApplication {

    private static final Logger LOGGER = LogManager.getLogger(StandardOfCareApplication.class);

    private static final String APPLICATION = "ACTIN Standard of Care";
    private static final String VERSION = StandardOfCareApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = StandardOfCareConfig.createOptions();

        StandardOfCareConfig config = null;
        try {
            config = StandardOfCareConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new StandardOfCareApplication(config).run();
    }

    @NotNull
    private final StandardOfCareConfig config;

    private StandardOfCareApplication(@NotNull final StandardOfCareConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading clinical record from {}", config.clinicalJson());
        ClinicalRecord clinical = ClinicalRecordJson.read(config.clinicalJson());
        ClinicalPrinter.printRecord(clinical);

        LOGGER.info("Loading molecular record from {}", config.molecularJson());
        MolecularRecord molecular = MolecularRecordJson.read(config.molecularJson());
        MolecularPrinter.printRecord(molecular);

        PatientRecord patient = PatientRecordFactory.fromInputs(clinical, molecular);

        LOGGER.info("Loading DOID tree from {}", config.doidJson());
        DoidEntry doidEntry = DoidJson.readDoidOwlEntry(config.doidJson());
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size());

        DoidModel doidModel = DoidModelFactory.createFromDoidEntry(doidEntry);
        ReferenceDateProvider referenceDateProvider = ReferenceDateProviderFactory.create(clinical, config.runHistorically());

        LOGGER.info("Done!");
    }
}
