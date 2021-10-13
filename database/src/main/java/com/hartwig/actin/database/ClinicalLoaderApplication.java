package com.hartwig.actin.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.hartwig.actin.database.dao.DatabaseAccess;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.serialization.ClinicalRecordJson;

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
            new HelpFormatter().printHelp("ACTIN Clinical Loader", options);
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
        List<ClinicalRecord> records = ClinicalRecordJson.read(config.clinicalDirectory());
        LOGGER.info(" Loaded {} clinical records", records.size());

        DatabaseAccess access = DatabaseAccess.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl());
        LOGGER.info("Writing {} clinical records to database", records.size());
        access.writeClinicalRecords(records);

        LOGGER.info("Done!");
    }
}
