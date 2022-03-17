package com.hartwig.actin.database.treatment;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.hartwig.actin.database.dao.DatabaseAccess;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.serialization.TrialJson;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TreatmentLoaderApplication {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentLoaderApplication.class);

    private static final String APPLICATION = "ACTIN Treatment Loader";
    private static final String VERSION = TreatmentLoaderApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException, SQLException {
        Options options = TreatmentLoaderConfig.createOptions();

        TreatmentLoaderConfig config = null;
        try {
            config = TreatmentLoaderConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new TreatmentLoaderApplication(config).run();
    }

    @NotNull
    private final TreatmentLoaderConfig config;

    private TreatmentLoaderApplication(@NotNull final TreatmentLoaderConfig config) {
        this.config = config;
    }

    public void run() throws IOException, SQLException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading trials from {}", config.treatmentDatabaseDirectory());
        List<Trial> trials = TrialJson.readFromDir(config.treatmentDatabaseDirectory());
        LOGGER.info(" Loaded {} trials", trials.size());

        DatabaseAccess access = DatabaseAccess.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl());
        LOGGER.info("Writing {} trials to database", trials.size());
        access.writeTrials(trials);

        LOGGER.info("Done!");
    }
}
