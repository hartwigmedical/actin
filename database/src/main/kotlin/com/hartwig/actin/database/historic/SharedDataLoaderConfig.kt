package com.hartwig.actin.database.historic

import com.hartwig.actin.database.DatabaseLoaderConfig
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator

data class SharedDataLoaderConfig(
    val sharedDataDirectory: String,
    override val dbUser: String,
    override val dbPass: String,
    override val dbUrl: String
) : DatabaseLoaderConfig {

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(SHARED_DATA_DIRECTORY, true, "Directory containing the shared data to be loaded")
            options.addOption(DB_USER, true, "Database username")
            options.addOption(DB_PASS, true, "Database password")
            options.addOption(DB_URL, true, "Database url")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): SharedDataLoaderConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }

            return SharedDataLoaderConfig(
                sharedDataDirectory = ApplicationConfig.nonOptionalDir(cmd, SHARED_DATA_DIRECTORY),
                dbUser = ApplicationConfig.nonOptionalValue(cmd, DB_USER),
                dbPass = ApplicationConfig.nonOptionalValue(cmd, DB_PASS),
                dbUrl = ApplicationConfig.nonOptionalValue(cmd, DB_URL)
            )
        }

        private val LOGGER = LogManager.getLogger(SharedDataLoaderConfig::class.java)
        private const val SHARED_DATA_DIRECTORY = "shared_data_directory"
        private const val DB_USER = "db_user"
        private const val DB_PASS = "db_pass"
        private const val DB_URL = "db_url"
        private const val LOG_DEBUG = "log_debug"
    }
}