package com.hartwig.actin.database.clinical;

import com.hartwig.actin.database.DatabaseLoaderConfig;
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
public interface ClinicalLoaderConfig extends DatabaseLoaderConfig {

    Logger LOGGER = LogManager.getLogger(ClinicalLoaderConfig.class);

    String CLINICAL_DIRECTORY = "clinical_directory";

    String DB_USER = "db_user";
    String DB_PASS = "db_pass";
    String DB_URL = "db_url";

    String LOG_DEBUG = "log_debug";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(CLINICAL_DIRECTORY, true, "Directory containing the clinical JSON files to load up");

        options.addOption(DB_USER, true, "Database username");
        options.addOption(DB_PASS, true, "Database password");
        options.addOption(DB_URL, true, "Database url");

        options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled");

        return options;
    }

    @NotNull
    String clinicalDirectory();

    @NotNull
    @Override
    String dbUser();

    @NotNull
    @Override
    String dbPass();

    @NotNull
    @Override
    String dbUrl();

    @NotNull
    static ClinicalLoaderConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        if (cmd.hasOption(LOG_DEBUG)) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.debug("Switched root level logging to DEBUG");
        }

        return ImmutableClinicalLoaderConfig.builder()
                .clinicalDirectory(ApplicationConfig.nonOptionalDir(cmd, CLINICAL_DIRECTORY))
                .dbUser(ApplicationConfig.nonOptionalValue(cmd, DB_USER))
                .dbPass(ApplicationConfig.nonOptionalValue(cmd, DB_PASS))
                .dbUrl(ApplicationConfig.nonOptionalValue(cmd, DB_URL))
                .build();
    }
}
