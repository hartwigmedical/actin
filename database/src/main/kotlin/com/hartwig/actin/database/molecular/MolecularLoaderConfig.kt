package com.hartwig.actin.database.molecular

import com.hartwig.actin.database.DatabaseLoaderConfig
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
interface MolecularLoaderConfig : DatabaseLoaderConfig {
    fun molecularJson(): String
    override fun dbUser(): String
    override fun dbPass(): String
    override fun dbUrl(): String

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(MOLECULAR_JSON, true, "Path of the molecular json to load up")
            options.addOption(DB_USER, true, "Database username")
            options.addOption(DB_PASS, true, "Database password")
            options.addOption(DB_URL, true, "Database url")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        @Throws(ParseException::class)
        fun createConfig(cmd: CommandLine): MolecularLoaderConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return ImmutableMolecularLoaderConfig.builder()
                .molecularJson(ApplicationConfig.nonOptionalFile(cmd, MOLECULAR_JSON))
                .dbUser(ApplicationConfig.nonOptionalValue(cmd, DB_USER))
                .dbPass(ApplicationConfig.nonOptionalValue(cmd, DB_PASS))
                .dbUrl(ApplicationConfig.nonOptionalValue(cmd, DB_URL))
                .build()
        }

        val LOGGER = LogManager.getLogger(MolecularLoaderConfig::class.java)
        const val MOLECULAR_JSON = "molecular_json"
        const val DB_USER = "db_user"
        const val DB_PASS = "db_pass"
        const val DB_URL = "db_url"
        const val LOG_DEBUG = "log_debug"
    }
}