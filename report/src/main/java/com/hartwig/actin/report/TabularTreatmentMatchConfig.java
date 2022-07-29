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
public interface TabularTreatmentMatchConfig {

    Logger LOGGER = LogManager.getLogger(ReporterConfig.class);

    String TREATMENT_MATCH_JSON = "treatment_match_json";
    String OUTPUT_TSV = "output_tsv";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(TREATMENT_MATCH_JSON, true, "File containing all available treatments, matched to the sample");

        options.addOption(OUTPUT_TSV, true, "TSV where the output will be written to");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String treatmentMatchJson();

    @NotNull
    String outputTsv();

    @NotNull
    static TabularTreatmentMatchConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableTabularTreatmentMatchConfig.builder()
                .treatmentMatchJson(ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON))
                .outputTsv(ApplicationConfig.nonOptionalValue(cmd, OUTPUT_TSV))
                .build();
    }
}
