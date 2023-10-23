package com.hartwig.actin.database.algo

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.database.dao.DatabaseAccess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.sql.SQLException

class TreatmentMatchLoaderApplication private constructor(private val config: TreatmentMatchLoaderConfig) {
    @Throws(IOException::class, SQLException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson())
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson())
        val access: DatabaseAccess = DatabaseAccess.Companion.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl())
        LOGGER.info("Writing treatment match results for {}", treatmentMatch.patientId())
        access.writeTreatmentMatch(treatmentMatch)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            TreatmentMatchLoaderApplication::class.java
        )
        private const val APPLICATION = "ACTIN Treatment Match Loader"
        private val VERSION = TreatmentMatchLoaderApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class, SQLException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = TreatmentMatchLoaderConfig.Companion.createOptions()
            var config: TreatmentMatchLoaderConfig? = null
            try {
                config = TreatmentMatchLoaderConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            TreatmentMatchLoaderApplication(config!!).run()
        }
    }
}