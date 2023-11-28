package com.hartwig.actin.database.trial

import com.hartwig.actin.database.DatabaseLoaderConfig
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator

data class TrialLoaderConfig(
    val trialDatabaseDirectory: String,
    override val dbUser: String,
    override val dbPass: String,
    override val dbUrl: String
) : DatabaseLoaderConfig {

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(TRIAL_DATABASE_DIRECTORY, true, "Directory containing all the trials that are expected to be loaded")
            options.addOption(DB_USER, true, "Database username")
            options.addOption(DB_PASS, true, "Database password")
            options.addOption(DB_URL, true, "Database url")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): TrialLoaderConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return TrialLoaderConfig(
                trialDatabaseDirectory = ApplicationConfig.nonOptionalDir(cmd, TRIAL_DATABASE_DIRECTORY),
                dbUser = ApplicationConfig.nonOptionalValue(cmd, DB_USER),
                dbPass = ApplicationConfig.nonOptionalValue(cmd, DB_PASS),
                dbUrl = ApplicationConfig.nonOptionalValue(cmd, DB_URL)
            )
        }

        private val LOGGER = LogManager.getLogger(TrialLoaderConfig::class.java)
        private const val TRIAL_DATABASE_DIRECTORY = "trial_database_directory"
        private const val DB_USER = "db_user"
        private const val DB_PASS = "db_pass"
        private const val DB_URL = "db_url"
        private const val LOG_DEBUG = "log_debug"
    }
}