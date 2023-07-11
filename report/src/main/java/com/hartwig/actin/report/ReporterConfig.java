package com.hartwig.actin.report;

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
public interface ReporterConfig {

    Logger LOGGER = LogManager.getLogger(ReporterConfig.class);

    String CLINICAL_JSON = "clinical_json";
    String MOLECULAR_JSON = "molecular_json";
    String TREATMENT_MATCH_JSON = "treatment_match_json";

    String OUTPUT_DIRECTORY = "output_directory";

    String ENABLE_EXTENDED_MODE = "enable_extended_mode";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the patient");
        options.addOption(MOLECULAR_JSON, true, "File containing the most recent molecular record of the patient");
        options.addOption(TREATMENT_MATCH_JSON, true, "File containing all available treatments, matched to the patient");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where the report will be written to");

        options.addOption(ENABLE_EXTENDED_MODE, false, "If set, includes trial matching details");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String clinicalJson();

    @NotNull
    String molecularJson();

    @NotNull
    String treatmentMatchJson();

    @NotNull
    String outputDirectory();

    boolean enableExtendedMode();

    @NotNull
    static ReporterConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        boolean enableExtendedMode = cmd.hasOption(ENABLE_EXTENDED_MODE);
        if (enableExtendedMode) {
            LOGGER.info("Extended reporting mode has been enabled");
        }

        return ImmutableReporterConfig.builder()
                .clinicalJson(ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON))
                .molecularJson(ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON))
                .treatmentMatchJson(ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .enableExtendedMode(enableExtendedMode)
                .build();
    }
}
