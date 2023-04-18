package com.hartwig.actin.algo.soc;

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
public interface StandardOfCareConfig {

    Logger LOGGER = LogManager.getLogger(StandardOfCareConfig.class);

    String CLINICAL_JSON = "clinical_json";
    String MOLECULAR_JSON = "molecular_json";

    String DOID_JSON = "doid_json";

    String RUN_HISTORICALLY = "run_historically";
    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the patient");
        options.addOption(MOLECULAR_JSON, true, "File containing the most recent molecular record of the patient");

        options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.");

        options.addOption(RUN_HISTORICALLY, false, "If set, runs the algo with a date just after the original patient registration date");
        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String clinicalJson();

    @NotNull
    String molecularJson();

    @NotNull
    String doidJson();

    boolean runHistorically();

    @NotNull
    static StandardOfCareConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        boolean runHistorically = cmd.hasOption(RUN_HISTORICALLY);
        if (runHistorically) {
            LOGGER.info("Configured to run in historic mode");
        }

        return ImmutableStandardOfCareConfig.builder()
                .clinicalJson(ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON))
                .molecularJson(ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON))
                .doidJson(ApplicationConfig.nonOptionalFile(cmd, DOID_JSON))
                .runHistorically(runHistorically)
                .build();
    }
}
