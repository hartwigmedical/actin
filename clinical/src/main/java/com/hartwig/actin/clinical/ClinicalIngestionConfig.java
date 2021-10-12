package com.hartwig.actin.clinical;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface ClinicalIngestionConfig {

    String FEED_DIRECTORY = "feed_directory";
    String CURATION_DIRECTORY = "curation_directory";

    String OUTPUT_DIRECTORY = "output_directory";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(FEED_DIRECTORY, true, "Directory containing the clinical feed data");
        options.addOption(CURATION_DIRECTORY, true, "Directory containing the clinical curation config data");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to");

        return options;
    }

    @NotNull
    String feedDirectory();

    @NotNull
    String curationDirectory();

    @NotNull
    String outputDirectory();

    @NotNull
    static ClinicalIngestionConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return ImmutableClinicalIngestionConfig.builder()
                .feedDirectory(nonOptionalDir(cmd, FEED_DIRECTORY))
                .curationDirectory(nonOptionalDir(cmd, CURATION_DIRECTORY))
                .outputDirectory(nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }

    @NotNull
    static String nonOptionalDir(@NotNull CommandLine cmd, @NotNull String param) throws ParseException {
        String value = nonOptionalValue(cmd, param);

        if (!pathExists(value) || !pathIsDirectory(value)) {
            throw new ParseException("Parameter '" + param + "' must be an existing directory: " + value);
        }

        return value;
    }

    @NotNull
    static String nonOptionalValue(@NotNull CommandLine cmd, @NotNull String param) throws ParseException {
        String value = cmd.getOptionValue(param);
        if (value == null) {
            throw new ParseException("Parameter must be provided: " + param);
        }

        return value;
    }

    static boolean pathExists(@NotNull String path) {
        return Files.exists(new File(path).toPath());
    }

    static boolean pathIsDirectory(@NotNull String path) {
        return Files.isDirectory(new File(path).toPath());
    }
}
