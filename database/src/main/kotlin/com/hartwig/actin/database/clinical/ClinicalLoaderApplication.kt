package com.hartwig.actin.database.clinical

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.database.dao.DatabaseAccess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class ClinicalLoaderApplication(private val config: ClinicalLoaderConfig) {

    fun run() {
        logger.info { "Running $APPLICATION v$VERSION" }

        logger.info { "Loading clinical model from ${config.clinicalDirectory}" }
        val records = ClinicalRecordJson.readFromDir(config.clinicalDirectory)

        logger.info { " Loaded ${records.size} clinical records" }
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        logger.info { "Writing ${records.size} clinical records to database" }
        access.writeClinicalRecords(records)

        logger.info { "Done!" }
    }

    companion object {
        const val APPLICATION = "ACTIN Clinical Loader"

        val logger = KotlinLogging.logger {}
        private val VERSION = ClinicalLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = ClinicalLoaderConfig.createOptions()
    val config: ClinicalLoaderConfig
    try {
        config = ClinicalLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        ClinicalLoaderApplication.logger.warn(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(ClinicalLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    ClinicalLoaderApplication(config).run()
}
