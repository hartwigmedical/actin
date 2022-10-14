package com.hartwig.actin.ckb;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.ckb.datamodel.CkbEntry;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class CkbDatabaseReaderApplication {

    private static final Logger LOGGER = LogManager.getLogger(CkbDatabaseReaderApplication.class);

    private static final String APPLICATION = "ACTIN CKB-Database Reader";
    private static final String VERSION = CkbDatabaseReaderApplication.class.getPackage().getImplementationVersion();

    public static void main(String[] args) throws IOException {
        Options options = CkbDatabaseReaderApplicationConfig.createOptions();

        CkbDatabaseReaderApplicationConfig config = null;
        try {
            config = CkbDatabaseReaderApplicationConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new CkbDatabaseReaderApplication(config).run();
    }

    @NotNull
    private final CkbDatabaseReaderApplicationConfig config;

    private CkbDatabaseReaderApplication(@NotNull final CkbDatabaseReaderApplicationConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Reading CKB-FLEX database from {}", config.ckbFlexDirectory());
        List<CkbEntry> entries = CkbEntryReader.read(config.ckbFlexDirectory());

        LOGGER.info(" Read {} entries", entries.size());

        LOGGER.info("Done!");
    }
}
