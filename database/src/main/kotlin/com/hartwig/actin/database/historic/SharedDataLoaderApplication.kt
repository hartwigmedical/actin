package com.hartwig.actin.database.historic

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.database.historic.serialization.HistoricClinicalDeserializer
import com.hartwig.actin.database.historic.serialization.HistoricMolecularDeserializer
import com.hartwig.actin.database.historic.serialization.HistoricTreatmentMatchDeserializer
import com.hartwig.actin.database.molecular.MolecularLoaderApplication
import com.hartwig.actin.molecular.datamodel.MolecularHistory
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
        val historicData: List<Triple<ClinicalRecord?, MolecularHistory?, TreatmentMatch?>> =
            File(config.sharedDataDirectory).list()!!.map {
                LOGGER.info(" Processing {}", it)

                val clinicalJson = findClinicalJson(it)
                val molecularJson = findMolecularJson(it)
                val treatmentMatchJson = findTreatmentMatchJson(it)

                val clinical: ClinicalRecord? =
                    if (clinicalJson.exists()) HistoricClinicalDeserializer.deserialize(clinicalJson) else null
                if (clinical == null) {
                    LOGGER.warn("Clinical record could not be constructed for {} based on : {}", it, clinicalJson)
                }

                val molecular: MolecularHistory? =
                    if (molecularJson.exists()) HistoricMolecularDeserializer.deserialize(molecularJson) else null
                if (molecular == null) {
                    LOGGER.warn("Molecular record could not be constructed for {} based on : {}", it, molecularJson)
                }

                val treatmentMatch: TreatmentMatch? =
                    if (treatmentMatchJson.exists()) HistoricTreatmentMatchDeserializer.deserialize(treatmentMatchJson) else null
                if (treatmentMatch == null) {
                    LOGGER.warn("Treatment match record could not be constructed for {} based on : {}", it, treatmentMatchJson)
                }

                Triple(clinical, molecular, treatmentMatch)
            }

        val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

        LOGGER.info("Writing clinical data for {} historic patients", historicData.size)
        access.writeClinicalRecords(historicData.mapNotNull { it.first })

        LOGGER.info("Writing molecular data for {} historic patients", historicData.size)
        historicData.mapNotNull { it -> it.second?.latestOrangeMolecularRecord()?.let { access.writeMolecularRecord(it) } }

        LOGGER.info("Writing treatment match data for {} historic patients", historicData.size)
        historicData.mapNotNull { it -> it.third?.let { access.writeTreatmentMatch(it) } }

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
        return config.sharedDataDirectory + File.separator + directory + File.separator + "actin" + File.separator + "1" + File.separator
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MolecularLoaderApplication::class.java)
        const val APPLICATION = "ACTIN Shared Data Loader"

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