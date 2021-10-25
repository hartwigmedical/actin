package com.hartwig.actin.treatment;

import com.hartwig.actin.util.ApplicationConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface TreatmentCreatorConfig {

    String TREATMENT_CONFIG_DIRECTORY = "treatment_config_directory";

    String OUTPUT_DIRECTORY = "output_directory";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(TREATMENT_CONFIG_DIRECTORY, true, "Directory containing the treatment config files");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where treatment data will be written to");

        return options;
    }

    @NotNull
    String treatmentConfigDirectory();

    @NotNull
    String outputDirectory();

    @NotNull
    static TreatmentCreatorConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return ImmutableTreatmentCreatorConfig.builder()
                .treatmentConfigDirectory(ApplicationConfig.nonOptionalDir(cmd, TREATMENT_CONFIG_DIRECTORY))
                .outputDirectory(ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
