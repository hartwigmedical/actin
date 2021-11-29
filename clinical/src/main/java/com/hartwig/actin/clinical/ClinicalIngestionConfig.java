package com.hartwig.actin.clinical;

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
public interface ClinicalIngestionConfig {

    Logger LOGGER = LogManager.getLogger(ClinicalIngestionConfig.class);

    String FEED_DIRECTORY = "feed_directory";
    String CURATION_DIRECTORY = "curation_directory";

    String OUTPUT_DIRECTORY = "output_directory";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(FEED_DIRECTORY, true, "Directory containing the clinical feed data");
        options.addOption(CURATION_DIRECTORY, true, "Directory containing the clinical curation config data");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String feedDirectory();

    @NotNull
    String curationDirectory();

    @NotNull
    String outputDirectory();

    @NotNull
    static ClinicalIngestionConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableClinicalIngestionConfig.builder()
                .feedDirectory(ApplicationConfig.nonOptionalDir(cmd, FEED_DIRECTORY))
                .curationDirectory(ApplicationConfig.nonOptionalDir(cmd, CURATION_DIRECTORY))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
