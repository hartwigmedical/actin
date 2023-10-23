package com.hartwig.actin.database.clinical

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.database.dao.DatabaseAccess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.sql.SQLException

class ClinicalLoaderApplication private constructor(private val config: ClinicalLoaderConfig) {
    @Throws(IOException::class, SQLException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading clinical model from {}", config.clinicalDirectory())
        val records = ClinicalRecordJson.readFromDir(config.clinicalDirectory())
        LOGGER.info(" Loaded {} clinical records", records.size)
        val access: DatabaseAccess = DatabaseAccess.Companion.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl())
        LOGGER.info("Writing {} clinical records to database", records.size)
        access.writeClinicalRecords(records)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalLoaderApplication::class.java)
        private const val APPLICATION = "ACTIN Clinical Loader"
        private val VERSION = ClinicalLoaderApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class, SQLException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = ClinicalLoaderConfig.Companion.createOptions()
            var config: ClinicalLoaderConfig? = null
            try {
                config = ClinicalLoaderConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            ClinicalLoaderApplication(config!!).run()
        }
    }
}