package com.hartwig.actin.database;

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
public interface DatabaseLoaderConfig {

    String CLINICAL_FEED_DIRECTORY = "clinical_feed_directory";
    String CLINICAL_CURATION_DIRECTORY = "clinical_curation_directory";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_FEED_DIRECTORY, true, "Directory containing the clinical feed data");
        options.addOption(CLINICAL_CURATION_DIRECTORY, true, "Directory containing the clinical curation config data");

        return options;
    }

    @NotNull
    String clinicalFeedDirectory();

    @NotNull
    String clinicalCurationDirectory();

    @NotNull
    static DatabaseLoaderConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return ImmutableDatabaseLoaderConfig.builder()
                .clinicalFeedDirectory(nonOptionalDir(cmd, CLINICAL_FEED_DIRECTORY))
                .clinicalCurationDirectory(nonOptionalDir(cmd, CLINICAL_CURATION_DIRECTORY))
                .build();
    }

    @NotNull
    static String nonOptionalValue(@NotNull CommandLine cmd, @NotNull String param) throws ParseException {
        String value = cmd.getOptionValue(param);
        if (value == null) {
            throw new ParseException("Parameter must be provided: " + param);
        }

        return value;
    }

    @NotNull
    static String nonOptionalDir(@NotNull CommandLine cmd, @NotNull String param) throws ParseException {
        String value = nonOptionalValue(cmd, param);

        if (!pathExists(value) || !pathIsDirectory(value)) {
            throw new ParseException("Parameter '" + param + "' must be an existing directory: " + value);
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
