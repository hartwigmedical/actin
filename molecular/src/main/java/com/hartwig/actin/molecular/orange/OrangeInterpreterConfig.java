package com.hartwig.actin.molecular.orange;

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
public interface OrangeInterpreterConfig {

    Logger LOGGER = LogManager.getLogger(OrangeInterpreterConfig.class);

    String ORANGE_JSON = "orange_json";
    String TREATMENT_DATABASE_DIRECTORY = "treatment_database_directory";

    String OUTPUT_DIRECTORY = "output_directory";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(ORANGE_JSON, true, "Path of the ORANGE json to be interpreted");
        options.addOption(TREATMENT_DATABASE_DIRECTORY, true, "Directory containing all available treatments");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where molecular data output will be written to");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String orangeJson();

    @NotNull
    String treatmentDatabaseDirectory();

    @NotNull
    String outputDirectory();

    @NotNull
    static OrangeInterpreterConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableOrangeInterpreterConfig.builder()
                .orangeJson(ApplicationConfig.nonOptionalFile(cmd, ORANGE_JSON))
                .treatmentDatabaseDirectory(ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DATABASE_DIRECTORY))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
