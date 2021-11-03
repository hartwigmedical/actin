package com.hartwig.actin.algo;

import com.hartwig.actin.util.ApplicationConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface TreatmentMatcherConfig {

    String CLINICAL_JSON = "clinical_json";
    String MOLECULAR_JSON = "molecular_json";
    String TREATMENT_DATABASE_DIRECTORY = "treatment_database_directory";

    String OUTPUT_DIRECTORY = "output_directory";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the sample");
        options.addOption(MOLECULAR_JSON, true, "File containing the molecular record of the sample");
        options.addOption(TREATMENT_DATABASE_DIRECTORY, true, "Directory containing all available treatments");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where the matcher output will be written to");

        return options;
    }

    @NotNull
    String clinicalJson();

    @NotNull
    String molecularJson();

    @NotNull
    String treatmentDatabaseDirectory();

    @NotNull
    String outputDirectory();

    @NotNull
    static TreatmentMatcherConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return ImmutableTreatmentMatcherConfig.builder()
                .clinicalJson(ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON))
                .molecularJson(ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON))
                .treatmentDatabaseDirectory(ApplicationConfig.nonOptionalDir(cmd, TREATMENT_DATABASE_DIRECTORY))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
