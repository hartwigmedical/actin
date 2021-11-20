package com.hartwig.actin.treatment;

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
public interface TreatmentCreatorConfig {

    Logger LOGGER = LogManager.getLogger(TreatmentCreatorConfig.class);

    String TRIAL_CONFIG_DIRECTORY = "trial_config_directory";

    String OUTPUT_DIRECTORY = "output_directory";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(TRIAL_CONFIG_DIRECTORY, true, "Directory containing the trial config files");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where treatment data will be written to");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String trialConfigDirectory();

    @NotNull
    String outputDirectory();

    @NotNull
    static TreatmentCreatorConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableTreatmentCreatorConfig.builder()
                .trialConfigDirectory(ApplicationConfig.nonOptionalDir(cmd, TRIAL_CONFIG_DIRECTORY))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
