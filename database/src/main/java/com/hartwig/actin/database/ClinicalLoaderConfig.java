package com.hartwig.actin.database;

import com.hartwig.actin.util.ApplicationConfig;

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
                .clinicalDirectory(ApplicationConfig.nonOptionalDir(cmd, CLINICAL_DIRECTORY))
                .dbUser(ApplicationConfig.nonOptionalValue(cmd, DB_USER))
                .dbPass(ApplicationConfig.nonOptionalValue(cmd, DB_PASS))
                .dbUrl(ApplicationConfig.nonOptionalValue(cmd, DB_URL))
                .build();
    }
}
