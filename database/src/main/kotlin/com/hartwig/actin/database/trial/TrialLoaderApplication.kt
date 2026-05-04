package com.hartwig.actin.database.trial

import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.trial.serialization.TrialJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class TrialLoaderApplication(private val config: TrialLoaderConfig) {

    fun run() {
        logger.info { "Running $APPLICATION v$VERSION" }

        logger.info { "Loading trials from ${config.trialDatabaseDirectory}" }
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory)

        logger.info { " Loaded ${trials.size} trials" }
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        logger.info { "Writing ${trials.size} trials to database" }
        access.writeTrials(trials)

        logger.info { "Done!" }
    }

    companion object {
        const val APPLICATION = "ACTIN Trial Loader"

        val logger = KotlinLogging.logger {}
        private val VERSION = TrialLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TrialLoaderConfig.createOptions()
    val config: TrialLoaderConfig
    try {
        config = TrialLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TrialLoaderApplication.logger.warn(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(TrialLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    TrialLoaderApplication(config).run()
}
