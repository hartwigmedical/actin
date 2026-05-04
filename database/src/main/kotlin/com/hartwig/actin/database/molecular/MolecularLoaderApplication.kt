package com.hartwig.actin.database.molecular

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class MolecularLoaderApplication(private val config: MolecularLoaderConfig) {

    fun run() {
        logger.info { "Running $APPLICATION v$VERSION" }

        logger.info { "Loading patient record from ${config.patientJson}" }
        val patientRecord = PatientRecordJson.read(config.patientJson)

        MolecularHistory(patientRecord.molecularTests).latestOrangeMolecularRecord()?.let { molecularTest ->
            val access = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

            logger.info { "Writing molecular test for ${molecularTest.sampleId}" }
            access.writeMolecularTest(patientRecord.patientId, molecularTest)

            logger.info { "Done!" }
        } ?: logger.warn { "No WGS record found in molecular history for ${patientRecord.patientId}" }
    }

    companion object {
        const val APPLICATION = "ACTIN Molecular Loader"

        val logger = KotlinLogging.logger {}
        private val VERSION = MolecularLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = MolecularLoaderConfig.createOptions()
    val config: MolecularLoaderConfig
    try {
        config = MolecularLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        MolecularLoaderApplication.logger.warn(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(MolecularLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    MolecularLoaderApplication(config).run()
}
