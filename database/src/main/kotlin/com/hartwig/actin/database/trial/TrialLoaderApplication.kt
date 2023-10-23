package com.hartwig.actin.database.trial

import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.treatment.serialization.TrialJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.sql.SQLException

class TrialLoaderApplication private constructor(private val config: TrialLoaderConfig) {
    @Throws(IOException::class, SQLException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory())
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory())
        LOGGER.info(" Loaded {} trials", trials.size)
        val access: DatabaseAccess = DatabaseAccess.Companion.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl())
        LOGGER.info("Writing {} trials to database", trials.size)
        access.writeTrials(trials)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialLoaderApplication::class.java)
        private const val APPLICATION = "ACTIN Trial Loader"
        private val VERSION = TrialLoaderApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class, SQLException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = TrialLoaderConfig.Companion.createOptions()
            var config: TrialLoaderConfig? = null
            try {
                config = TrialLoaderConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            TrialLoaderApplication(config!!).run()
        }
    }
}