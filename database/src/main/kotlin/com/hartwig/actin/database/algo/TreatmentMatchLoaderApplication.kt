package com.hartwig.actin.database.algo

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.database.dao.DatabaseAccess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class TreatmentMatchLoaderApplication(private val config: TreatmentMatchLoaderConfig) {

    fun run() {
        logger.info { "Running $APPLICATION v$VERSION" }

        logger.info { "Loading treatment match results from ${config.treatmentMatchJson}" }
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        logger.info { "Writing treatment match results for ${treatmentMatch.patientId}" }
        access.writeTreatmentMatch(treatmentMatch)
        logger.info { "Done!" }
    }

    companion object {
        const val APPLICATION = "ACTIN Treatment Match Loader"

        val logger = KotlinLogging.logger {}
        private val VERSION = TreatmentMatchLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentMatchLoaderConfig.createOptions()
    val config: TreatmentMatchLoaderConfig
    try {
        config = TreatmentMatchLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentMatchLoaderApplication.logger.warn(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(TreatmentMatchLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    TreatmentMatchLoaderApplication(config).run()
}
