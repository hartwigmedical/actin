package com.hartwig.actin.database.algo

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.database.dao.DatabaseAccess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class TreatmentMatchLoaderApplication(private val config: TreatmentMatchLoaderConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        LOGGER.info("Writing treatment match results for {}", treatmentMatch.patientId)
        access.writeTreatmentMatch(treatmentMatch)
        LOGGER.info("Done!")
    }

    companion object {
        const val APPLICATION = "ACTIN Treatment Match Loader"

        val LOGGER: Logger = LogManager.getLogger(TreatmentMatchLoaderApplication::class.java)
        private val VERSION = TreatmentMatchLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentMatchLoaderConfig.createOptions()
    val config: TreatmentMatchLoaderConfig
    try {
        config = TreatmentMatchLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentMatchLoaderApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(TreatmentMatchLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    TreatmentMatchLoaderApplication(config).run()
}
