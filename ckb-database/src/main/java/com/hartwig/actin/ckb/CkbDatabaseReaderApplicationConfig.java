package com.hartwig.actin.ckb;

import com.hartwig.actin.util.ApplicationConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface CkbDatabaseReaderApplicationConfig {

    Logger LOGGER = LogManager.getLogger(CkbDatabaseReaderApplicationConfig.class);

    String CKB_FLEX_DIRECTORY = "ckb_flex_directory";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CKB_FLEX_DIRECTORY, true, "Path to the CKB flex directory to be read");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String ckbFlexDirectory();

    @NotNull
    static CkbDatabaseReaderApplicationConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableCkbDatabaseReaderApplicationConfig.builder()
                .ckbFlexDirectory(ApplicationConfig.nonOptionalDir(cmd, CKB_FLEX_DIRECTORY))
                .build();
    }
}
