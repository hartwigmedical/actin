package com.hartwig.actin.database.algo;

import java.io.IOException;
import java.sql.SQLException;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.serialization.TreatmentMatchJson;
import com.hartwig.actin.database.dao.DatabaseAccess;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TreatmentMatchLoaderApplication {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentMatchLoaderApplication.class);

    private static final String APPLICATION = "ACTIN Treatment Match Loader";
    private static final String VERSION = TreatmentMatchLoaderApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException, SQLException {
        Options options = TreatmentMatchLoaderConfig.createOptions();

        TreatmentMatchLoaderConfig config = null;
        try {
            config = TreatmentMatchLoaderConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new TreatmentMatchLoaderApplication(config).run();
    }

    @NotNull
    private final TreatmentMatchLoaderConfig config;

    private TreatmentMatchLoaderApplication(@NotNull final TreatmentMatchLoaderConfig config) {
        this.config = config;
    }

    public void run() throws IOException, SQLException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson());
        TreatmentMatch treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson());

        DatabaseAccess access = DatabaseAccess.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl());
        LOGGER.info("Writing treatment match results for {}", treatmentMatch.patientId());
        access.writeTreatmentMatch(treatmentMatch);

        LOGGER.info("Done!");
    }
}
