package com.hartwig.actin.serve;

import java.io.IOException;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ServeBridgeApplication {

    private static final Logger LOGGER = LogManager.getLogger(ServeBridgeApplication.class);

    private static final String APPLICATION = "ACTIN SERVE-Bridge";
    public static final String VERSION = ServeBridgeApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = ServeBridgeConfig.createOptions();

        ServeBridgeConfig config = null;
        try {
            config = ServeBridgeConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new ServeBridgeApplication(config).run();
    }

    @NotNull
    private final ServeBridgeConfig config;

    private ServeBridgeApplication(@NotNull final ServeBridgeConfig config) {
        this.config = config;
    }

    public void run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Done!");
    }
}
