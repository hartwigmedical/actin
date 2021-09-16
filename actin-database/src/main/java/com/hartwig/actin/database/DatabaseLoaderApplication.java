package com.hartwig.actin.database;

import java.io.IOException;

import com.hartwig.actin.clinical.ClinicalModel;
import com.hartwig.actin.clinical.ClinicalModelFactory;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DatabaseLoaderApplication {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseLoaderApplication.class);

    public static final String VERSION = DatabaseLoaderApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        LOGGER.info("Running ACTIN Database Loader v{}", VERSION);

        Options options = DatabaseLoaderConfig.createOptions();

        DatabaseLoaderConfig config = null;
        try {
            config = DatabaseLoaderConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp("ACTIN DatabaseLoader", options);
            System.exit(1);
        }

        new DatabaseLoaderApplication(config).run();
    }

    @NotNull
    private final DatabaseLoaderConfig config;

    private DatabaseLoaderApplication(@NotNull final DatabaseLoaderConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        ClinicalModel clinicalModel = ClinicalModelFactory.loadFromClinicalDataDirectory(config.clinicalDataDirectory());

        LOGGER.debug("Create clinical model '{}'", clinicalModel);
        LOGGER.info("Done!");
    }
}
