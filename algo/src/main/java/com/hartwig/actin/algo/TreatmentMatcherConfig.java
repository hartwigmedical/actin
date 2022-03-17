package com.hartwig.actin.algo;

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
public interface TreatmentMatcherConfig {

    Logger LOGGER = LogManager.getLogger(TreatmentMatcherConfig.class);

    String CLINICAL_JSON = "clinical_json";
    String MOLECULAR_JSON = "molecular_json";

    String TREATMENT_DATABASE_DIRECTORY = "treatment_database_directory";
    String DOID_JSON = "doid_json";

    String OUTPUT_DIRECTORY = "output_directory";

    String RUN_HISTORICALLY = "run_historically";
    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the sample");
        options.addOption(MOLECULAR_JSON, true, "File containing the molecular record of the sample");

        options.addOption(TREATMENT_DATABASE_DIRECTORY, true, "Directory containing all available treatments");
        options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where the matcher output will be written to");

        options.addOption(RUN_HISTORICALLY, false, "If set, runs the algo with a date just after the original patient registration date");
        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String clinicalJson();

    @NotNull
    String molecularJson();

    @NotNull
    String treatmentDatabaseDirectory();

    @NotNull
    String doidJson();

    @NotNull
    String outputDirectory();

    boolean runHistorically();

    @NotNull
    static TreatmentMatcherConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        boolean runHistorically = cmd.hasOption(RUN_HISTORICALLY);
        if (runHistorically) {
            LOGGER.debug("Switched to run in historic mode");
        }

        return ImmutableTreatmentMatcherConfig.builder()
                .clinicalJson(ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON))
                .molecularJson(ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON))
                .treatmentDatabaseDirectory(ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DATABASE_DIRECTORY))
                .doidJson(ApplicationConfig.nonOptionalFile(cmd, DOID_JSON))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .runHistorically(runHistorically)
                .build();
    }
}
