package com.hartwig.actin.database.clinical

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.database.dao.DatabaseAccess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class ClinicalLoaderApplication(private val config: ClinicalLoaderConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical model from {}", config.clinicalDirectory)
        val records = ClinicalRecordJson.readFromDir(config.clinicalDirectory)

        LOGGER.info(" Loaded {} clinical records", records.size)
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        LOGGER.info("Writing {} clinical records to database", records.size)
        access.writeClinicalRecords(records)

        LOGGER.info("Done!")
    }

    companion object {
        const val APPLICATION = "ACTIN Clinical Loader"

        val LOGGER: Logger = LogManager.getLogger(ClinicalLoaderApplication::class.java)
        private val VERSION = ClinicalLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = ClinicalLoaderConfig.createOptions()
    val config: ClinicalLoaderConfig
    try {
        config = ClinicalLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        ClinicalLoaderApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(ClinicalLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    ClinicalLoaderApplication(config).run()
}
