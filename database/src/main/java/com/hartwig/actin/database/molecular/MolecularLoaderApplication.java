package com.hartwig.actin.database.molecular;

import java.io.IOException;
import java.sql.SQLException;

import com.hartwig.actin.database.dao.DatabaseAccess;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MolecularLoaderApplication {

    private static final Logger LOGGER = LogManager.getLogger(MolecularLoaderApplication.class);

    private static final String APPLICATION = "ACTIN Molecular Loader";
    private static final String VERSION = MolecularLoaderApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException, SQLException {
        Options options = MolecularLoaderConfig.createOptions();

        MolecularLoaderConfig config = null;
        try {
            config = MolecularLoaderConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new MolecularLoaderApplication(config).run();
    }

    @NotNull
    private final MolecularLoaderConfig config;

    private MolecularLoaderApplication(@NotNull final MolecularLoaderConfig config) {
        this.config = config;
    }

    public void run() throws IOException, SQLException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading molecular record from {}", config.molecularJson());
        MolecularRecord record = MolecularRecordJson.read(config.molecularJson());

        DatabaseAccess access = DatabaseAccess.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl());
        LOGGER.info("Writing molecular record for {}", record.sampleId());
        access.writeMolecularRecord(record);

        LOGGER.info("Done!");
    }
}
