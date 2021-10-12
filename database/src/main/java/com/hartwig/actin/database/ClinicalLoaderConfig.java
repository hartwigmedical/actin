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
public interface ClinicalLoaderConfig {

    String CLINICAL_DIRECTORY = "clinical_directory";

    String DB_USER = "db_user";
    String DB_PASS = "db_pass";
    String DB_URL = "db_url";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_DIRECTORY, true, "Directory containing the clinical JSON files to load up");

        options.addOption(DB_USER, true, "Database username");
        options.addOption(DB_PASS, true, "Database password");
        options.addOption(DB_URL, true, "Database url");

        return options;
    }

    @NotNull
    String clinicalDirectory();

    @NotNull
    String dbUser();

    @NotNull
    String dbPass();

    @NotNull
    String dbUrl();

    @NotNull
    static ClinicalLoaderConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return ImmutableClinicalLoaderConfig.builder()
                .clinicalDirectory(nonOptionalDir(cmd, CLINICAL_DIRECTORY))
                .dbUser(nonOptionalValue(cmd, DB_USER))
                .dbPass(nonOptionalValue(cmd, DB_PASS))
                .dbUrl(nonOptionalValue(cmd, DB_URL))
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
