package com.hartwig.actin.database.molecular

import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class MolecularLoaderApplication constructor(private val config: MolecularLoaderConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading molecular record from {}", config.molecularJson)
        val record = MolecularRecordJson.read(config.molecularJson)
        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)
        LOGGER.info("Writing molecular record for {}", record.sampleId())
        access.writeMolecularRecord(record)
        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MolecularLoaderApplication::class.java)
        const val APPLICATION = "ACTIN Molecular Loader"
        private val VERSION = MolecularLoaderApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>) {
    val options: Options = MolecularLoaderConfig.createOptions()
    try {
        val config = MolecularLoaderConfig.createConfig(DefaultParser().parse(options, args))
        MolecularLoaderApplication(config).run()
    } catch (exception: ParseException) {
        MolecularLoaderApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(MolecularLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }
}
