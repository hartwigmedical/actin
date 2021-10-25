package com.hartwig.actin.treatment;

import java.io.IOException;

import com.hartwig.actin.treatment.trial.TrialConfigDatabase;
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseReader;
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseValidator;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TreatmentCreatorApplication {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentCreatorApplication.class);

    private static final String APPLICATION = "ACTIN Treatment Creator";
    private static final String VERSION = TreatmentCreatorApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        Options options = TreatmentCreatorConfig.createOptions();

        TreatmentCreatorConfig config = null;
        try {
            config = TreatmentCreatorConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new TreatmentCreatorApplication(config).run();
    }

    @NotNull
    private final TreatmentCreatorConfig config;

    public TreatmentCreatorApplication(@NotNull final TreatmentCreatorConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        TrialConfigDatabase database = TrialConfigDatabaseReader.read(config.trialConfigDirectory());

        if (TrialConfigDatabaseValidator.isValid(database)) {
            String outputDirectory = config.outputDirectory();
            LOGGER.info("TODO: Write treatments to {}", outputDirectory);
        }

        LOGGER.info("Done!");
    }
}
