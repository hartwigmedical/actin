package com.hartwig.actin.database;

import java.io.IOException;
import java.sql.SQLException;

import com.hartwig.actin.database.dao.DatabaseAccess;
import com.hartwig.actin.datamodel.ClinicalModel;
import com.hartwig.actin.datamodel.ClinicalModelFile;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ClinicalLoaderApplication {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalLoaderApplication.class);

    private static final String VERSION = ClinicalLoaderApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException, SQLException {
        LOGGER.info("Running ACTIN Clinical Loader v{}", VERSION);

        Options options = ClinicalLoaderConfig.createOptions();

        ClinicalLoaderConfig config = null;
        try {
            config = ClinicalLoaderConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp("ACTIN DatabaseLoader", options);
            System.exit(1);
        }

        new ClinicalLoaderApplication(config).run();
    }

    @NotNull
    private final ClinicalLoaderConfig config;

    private ClinicalLoaderApplication(@NotNull final ClinicalLoaderConfig config) {
        this.config = config;
    }

    public void run() throws IOException, SQLException {
        LOGGER.info("Loading clinical model from {}", config.clinicalDirectory());
        ClinicalModel model = ClinicalModelFile.read(config.clinicalDirectory());
        LOGGER.info(" Loaded {} clinical records", model.records().size());

        DatabaseAccess access = DatabaseAccess.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl());
        LOGGER.info("Writing {} clinical records to database", model.records().size());
        access.writeClinicalRecords(model.records());

        LOGGER.info("Done!");
    }
}
