package com.hartwig.actin.database.trial

import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.trial.serialization.TrialJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class TrialLoaderApplication(private val config: TrialLoaderConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory)
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory)

        LOGGER.info(" Loaded {} trials", trials.size)
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        LOGGER.info("Writing {} trials to database", trials.size)
        access.writeTrials(trials)

        LOGGER.info("Done!")
    }

    companion object {
        const val APPLICATION = "ACTIN Trial Loader"

        val LOGGER: Logger = LogManager.getLogger(TrialLoaderApplication::class.java)
        private val VERSION = TrialLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TrialLoaderConfig.createOptions()
    val config: TrialLoaderConfig
    try {
        config = TrialLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TrialLoaderApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(TrialLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    TrialLoaderApplication(config).run()
}
