package com.hartwig.actin.serve;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface ServeBridgeConfig {

    String TREATMENT_DATABASE_DIRECTORY = "treatment_database_directory";

    String OUTPUT_SERVE_KNOWLEDGEBASE_TSV = "output_serve_knowledgebase_tsv";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(TREATMENT_DATABASE_DIRECTORY, true, "Directory containing all available treatments");

        options.addOption(OUTPUT_SERVE_KNOWLEDGEBASE_TSV, true, "Output TSV which will contain the SERVE knowledgebase");

        return options;
    }

    @NotNull
    String treatmentDatabaseDirectory();

    @NotNull
    String outputServeKnowledgebaseTsv();

    @NotNull
    static ServeBridgeConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return null;
    }
}
