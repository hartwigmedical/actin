package com.hartwig.actin.database.historic

import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.database.molecular.MolecularLoaderApplication
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.system.exitProcess

class SharedDataLoaderApplication(private val config: SharedDataLoaderConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Converting shared data records from {} to patient records", config.sharedDataDirectory)
        val patients = File(config.sharedDataDirectory).list()!!.map {
            LOGGER.info(" Processing {}", it)

            val clinical = findClinicalJson(it)
            val molecular = findMolecularJson(it)
            val treatmentMatch = findTreatmentMatchJson(it)

            if (!clinical.exists()) {
                LOGGER.warn("Clinical file does not exist: {}", clinical)
            }

            if (!molecular.exists()) {
                LOGGER.warn("Molecular file does not exist: {}", molecular)
            }

            if (!treatmentMatch.exists()) {
                LOGGER.warn("Treatment match file does not exist: {}", treatmentMatch)
            }
            "1"
        }

        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        LOGGER.info("Done!")
    }

    private fun findClinicalJson(directory: String): File {
        return File(sharedPath(directory) + directory + "T.clinical.json")
    }

    private fun findMolecularJson(directory: String): File {
        return File(sharedPath(directory) + directory + "T.molecular.json")
    }

    private fun findTreatmentMatchJson(directory: String): File {
        return File(sharedPath(directory) + directory + "T.treatment_match.json")
    }

    private fun sharedPath(directory: String): String {
        return config.sharedDataDirectory + File.pathSeparator + directory + File.pathSeparator + BASE_PATH + File.pathSeparator
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MolecularLoaderApplication::class.java)
        const val APPLICATION = "ACTIN Shared Data Loader"

        private val BASE_PATH = "actin" + File.pathSeparator + "1"
        private val VERSION = MolecularLoaderApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>) {
    val options: Options = SharedDataLoaderConfig.createOptions()
    try {
        val config = SharedDataLoaderConfig.createConfig(DefaultParser().parse(options, args))
        SharedDataLoaderApplication(config).run()
    } catch (exception: ParseException) {
        SharedDataLoaderApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(SharedDataLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }
}