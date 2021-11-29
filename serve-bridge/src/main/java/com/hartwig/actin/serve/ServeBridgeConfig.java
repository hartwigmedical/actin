package com.hartwig.actin.serve;

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
public interface ServeBridgeConfig {

    Logger LOGGER = LogManager.getLogger(ServeBridgeConfig.class);

    String TREATMENT_DATABASE_DIRECTORY = "treatment_database_directory";

    String OUTPUT_SERVE_KNOWLEDGEBASE_TSV = "output_serve_knowledgebase_tsv";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(TREATMENT_DATABASE_DIRECTORY, true, "Directory containing all available treatments");

        options.addOption(OUTPUT_SERVE_KNOWLEDGEBASE_TSV, true, "Output TSV which will contain the SERVE knowledgebase");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String treatmentDatabaseDirectory();

    @NotNull
    String outputServeKnowledgebaseTsv();

    @NotNull
    static ServeBridgeConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableServeBridgeConfig.builder()
                .treatmentDatabaseDirectory(ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DATABASE_DIRECTORY))
                .outputServeKnowledgebaseTsv(ApplicationConfig.nonOptionalValue(cmd, OUTPUT_SERVE_KNOWLEDGEBASE_TSV))
                .build();
    }
}
