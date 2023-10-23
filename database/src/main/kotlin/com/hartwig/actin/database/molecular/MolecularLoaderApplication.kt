package com.hartwig.actin.database.molecular

import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.sql.SQLException

class MolecularLoaderApplication private constructor(private val config: MolecularLoaderConfig) {
    @Throws(IOException::class, SQLException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading molecular record from {}", config.molecularJson())
        val record = MolecularRecordJson.read(config.molecularJson())
        val access: DatabaseAccess = DatabaseAccess.Companion.fromCredentials(config.dbUser(), config.dbPass(), config.dbUrl())
        LOGGER.info("Writing molecular record for {}", record.sampleId())
        access.writeMolecularRecord(record)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(MolecularLoaderApplication::class.java)
        private const val APPLICATION = "ACTIN Molecular Loader"
        private val VERSION = MolecularLoaderApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class, SQLException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = MolecularLoaderConfig.Companion.createOptions()
            var config: MolecularLoaderConfig? = null
            try {
                config = MolecularLoaderConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            MolecularLoaderApplication(config!!).run()
        }
    }
}