package com.hartwig.actin.database.algo

import com.hartwig.actin.database.DatabaseLoaderConfig
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator

data class TreatmentMatchLoaderConfig(
    val treatmentMatchJson: String, override val dbUser: String, override val dbPass: String, override val dbUrl: String
) : DatabaseLoaderConfig {

    companion object {
        private val LOGGER = LogManager.getLogger(TreatmentMatchLoaderConfig::class.java)

        private const val TREATMENT_MATCH_JSON = "treatment_match_json"
        private const val DB_USER = "db_user"
        private const val DB_PASS = "db_pass"
        private const val DB_URL = "db_url"
        private const val LOG_DEBUG = "log_debug"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(TREATMENT_MATCH_JSON, true, "File containing all available treatments, matched to the patient")
            options.addOption(DB_USER, true, "Database username")
            options.addOption(DB_PASS, true, "Database password")
            options.addOption(DB_URL, true, "Database url")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): TreatmentMatchLoaderConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return TreatmentMatchLoaderConfig(
                treatmentMatchJson = ApplicationConfig.nonOptionalFile(cmd, TREATMENT_MATCH_JSON),
                dbUser = ApplicationConfig.nonOptionalValue(cmd, DB_USER),
                dbPass = ApplicationConfig.nonOptionalValue(cmd, DB_PASS),
                dbUrl = ApplicationConfig.nonOptionalValue(cmd, DB_URL)
            )
        }
    }
}