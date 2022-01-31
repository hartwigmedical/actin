package com.hartwig.actin.molecular;

import java.io.IOException;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class MolecularIngestionApplication {

    private static final Logger LOGGER = LogManager.getLogger(MolecularIngestionApplication.class);

    private static final String APPLICATION = "ACTIN Molecular Ingestion";
    private static final String VERSION = MolecularIngestionApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = MolecularIngestionConfig.createOptions();

        MolecularIngestionConfig config = null;
        try {
            config = MolecularIngestionConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new MolecularIngestionApplication(config).run();
    }

    @NotNull
    private final MolecularIngestionConfig config;

    private MolecularIngestionApplication(@NotNull final MolecularIngestionConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Done!");
    }
}
