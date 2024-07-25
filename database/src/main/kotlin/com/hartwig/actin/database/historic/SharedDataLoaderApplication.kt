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

                val clinical: ClinicalRecord? = clinicalJson?.let { HistoricClinicalDeserializer.deserialize(clinicalJson) }
                if (clinical == null) {
                    LOGGER.warn("  Clinical record could not be constructed for {}", it)
                }

                val molecular: MolecularHistory? = molecularJson?.let { HistoricMolecularDeserializer.deserialize(molecularJson) }
                if (molecular == null) {
                    LOGGER.warn("  Molecular record could not be constructed for {}", it)
                }

                val treatmentMatch: TreatmentMatch? =
                    treatmentMatchJson?.let { HistoricTreatmentMatchDeserializer.deserialize(treatmentMatchJson) }
                if (treatmentMatch == null) {
                    LOGGER.warn("  Treatment match record could not be constructed for {}", it)
                }

                Triple(clinical, molecular, treatmentMatch)
            }

        if (config.writeDataToDb) {
            LOGGER.info("Connecting to {}", config.dbUrl)
            val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

            LOGGER.info("Writing clinical data for {} historic patients", historicData.size)
            access.writeClinicalRecords(historicData.mapNotNull { it.first })

            LOGGER.info("Writing molecular data for {} historic patients", historicData.size)
            historicData.mapNotNull { it -> it.second?.latestOrangeMolecularRecord()?.let { access.writeMolecularRecord(it) } }

            LOGGER.info("Writing treatment match data for {} historic patients", historicData.size)
            historicData.mapNotNull { it -> it.third?.let { access.writeTreatmentMatch(it) } }
        } else {
            LOGGER.info("Skipping database writing")
        }

        LOGGER.info("Done!")
    }

    private fun findClinicalJson(patient: String): File? {
        return findJson(patient, "clinical")
    }

    private fun findMolecularJson(patient: String): File? {
        return findJson(patient, "molecular")
    }

    private fun findTreatmentMatchJson(patient: String): File? {
        return findJson(patient, "treatment_match")
    }

    private fun findJson(patient: String, pattern: String): File? {
        latestSharedPath(patient)?.let {
            for (i in 9 downTo 1) {
                val sampleSuffix = if (i != 1) i else ""
                val sampleAttempt = File(it + patient + "T" + sampleSuffix + "." + pattern + ".json")
                if (sampleAttempt.exists()) {
                    return sampleAttempt
                }
            }

            val patientAttempt = File(latestSharedPath(patient) + patient + "." + pattern + ".json")
            if (patientAttempt.exists()) {
                return patientAttempt
            }
        }

        return null
    }

    private fun latestSharedPath(directory: String): String? {
        val basePath = config.sharedDataDirectory + File.separator + directory + File.separator + "actin" + File.separator
        for (i in 9 downTo 1) {
            val actinPath = basePath + i + File.separator
            if (File(actinPath).isDirectory) {
                return actinPath
            }
        }

        LOGGER.warn("  No ACTIN shared data path found in {}", directory)
        return null
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