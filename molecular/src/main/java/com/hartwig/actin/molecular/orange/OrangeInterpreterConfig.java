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

    // Params for clinical annotation and interpretation
    String SERVE_DIRECTORY = "serve_directory";
    String EXTERNAL_TRIAL_MAPPING_TSV = "external_trial_mapping_tsv";
    String CLINICAL_JSON = "clinical_json";
    String GENE_FILTER_TSV = "gene_filter_tsv";

    String OUTPUT_DIRECTORY = "output_directory";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(ORANGE_JSON, true, "Path of the ORANGE json to be interpreted");
        options.addOption(SERVE_DIRECTORY, true, "Path towards the SERVE directory containing known and actionable events");
        options.addOption(EXTERNAL_TRIAL_MAPPING_TSV, true, "A mapping from external trial names to ACTIN trials");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where molecular data output will be written to");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String orangeJson();

    @NotNull
    String serveDirectory();

    @NotNull
    String externalTrialMappingTsv();

    @NotNull
    String clinicalJson();

    @NotNull
    String geneFilterTsv();

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
                .serveDirectory(ApplicationConfig.nonOptionalDir(cmd, SERVE_DIRECTORY))
                .externalTrialMappingTsv(ApplicationConfig.nonOptionalFile(cmd, EXTERNAL_TRIAL_MAPPING_TSV))
                .clinicalJson(ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON))
                .geneFilterTsv(ApplicationConfig.nonOptionalFile(cmd, GENE_FILTER_TSV))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
