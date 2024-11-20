package com.hartwig.actin.database.historic

import com.google.gson.GsonBuilder
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.database.dao.DatabaseAccess
import com.hartwig.actin.database.historic.serialization.HistoricMolecularDeserializer
import com.hartwig.actin.database.historic.serialization.HistoricTreatmentMatchDeserializer
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import kotlin.system.exitProcess

class SharedDataLoaderApplication(private val config: SharedDataLoaderConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Converting shared data records from {} to patient records", config.sharedDataDirectory)
        val historicData: List<Pair<MolecularHistory?, TreatmentMatch?>> =
            File(config.sharedDataDirectory).list()!!.map {
                LOGGER.info(" Processing {}", it)

                val basePath = latestSharedPath(it)
                if (basePath != null) {
                    extractSinglePatientData(basePath, it)
                } else {
                    LOGGER.warn("  Could not find ACTIN shared data path for `{}`", it)
                    Pair(null, null)
                }
            }

        if (config.writeDataToDb) {
            LOGGER.info("Connecting to {}", config.dbUrl)
            val access: DatabaseAccess = DatabaseAccess.fromCredentials(config.dbUser, config.dbPass, config.dbUrl)

            writeHistoricToDatabase(access, historicData)
            loadLatestClinicalToDatabase(access, config.clinicalDirectory)
            loadLatestMolecularToDatabase(access, config.molecularDirectory)
        } else {
            LOGGER.info("Skipping database writing")
        }

        LOGGER.info("Done!")
    }

    private fun latestSharedPath(directory: String): String? {
        val basePath = sequenceOf(config.sharedDataDirectory, directory, "actin", "").joinToString(File.separator)
        return (9 downTo 1).map { i -> basePath + i + File.separator }
            .firstOrNull { actinPath -> File(actinPath).isDirectory }
    }

    private fun extractSinglePatientData(basePath: String, patient: String): Pair<MolecularHistory?, TreatmentMatch?> {
        return Pair(
            extractRecordFromFile(basePath, patient, "molecular", HistoricMolecularDeserializer::deserialize),
            extractRecordFromFile(basePath, patient, "treatment_match", HistoricTreatmentMatchDeserializer::deserialize)
        )
    }

    private fun <T> extractRecordFromFile(basePath: String, patient: String, pattern: String, deserialize: (File) -> T): T? {
        return findJson(basePath, patient, pattern)?.let(deserialize).also {
            if (it == null) {
                LOGGER.warn("  ${pattern.replace("_", " ").replaceFirstChar(Char::uppercase)} record could not be constructed for $patient")
            }
        }
    }

    private fun findJson(basePath: String, patient: String, pattern: String): File? {
        for (i in 9 downTo 1) {
            val sampleSuffix = if (i != 1) i else ""
            val sampleAttempt = File(basePath + patient + "T" + sampleSuffix + "." + pattern + ".json")
            if (sampleAttempt.exists()) {
                return sampleAttempt
            }
        }

        return File("$basePath$patient.$pattern.json").takeIf { it.exists() }
    }

    private fun writeHistoricToDatabase(
        access: DatabaseAccess,
        historicData: List<Pair<MolecularHistory?, TreatmentMatch?>>
    ) {
        LOGGER.info("Writing molecular data for {} historic patients", historicData.size)
        historicData.mapNotNull { it.first?.latestOrangeMolecularRecord() }.forEach(access::writeMolecularRecord)

        LOGGER.info("Writing treatment match data for {} historic patients", historicData.size)
        access.replaceTreatmentMatches(historicData.mapNotNull { it.second })
    }

    private fun loadLatestMolecularToDatabase(access: DatabaseAccess, molecularDirectory: String) {
        LOGGER.info("Loading molecular data from {}", molecularDirectory)
        val files = File(molecularDirectory).listFiles()
            ?: throw IllegalArgumentException("Could not retrieve patient json files from $molecularDirectory")

        val records = files.map {
            LOGGER.info("Loading molecular data from {}", it)
            Files.readString(it.toPath())
                .replace("\"type\":\"WHOLE_GENOME\"", "\"experimentType\":\"HARTWIG_WHOLE_GENOME\"")
                .replace("\"type\":\"IHC\"", "\"experimentType\":\"IHC\"")
                .let(PatientRecordJson::fromJson)
        }
            .map {
                requireNotNull(it.molecularHistory.latestOrangeMolecularRecord()) {
                    "No WGS record found in molecular history"
                }
            }

        LOGGER.info("Writing {} molecular records to database", records.size)
        val gson = GsonBuilder().serializeNulls()
            .enableComplexMapKeySerialization()
            .serializeSpecialFloatingPointValues()
            .create()

        records.forEach {
            val json = gson.toJson(it)
            val path = "$molecularDirectory/../molecular_out/"
            val jsonFile = path + it.patientId + ".patient_record.json"
            LOGGER.info("Writing patient record to {}", jsonFile)
            val writer = BufferedWriter(FileWriter(jsonFile))
            writer.write(json)
            writer.close()

            access.writeMolecularRecord(it)
        }
    }

    private fun loadLatestClinicalToDatabase(access: DatabaseAccess, clinicalDirectory: String) {
        LOGGER.info("Loading clinical data from {}", clinicalDirectory)
        val records = ClinicalRecordJson.readFromDir(clinicalDirectory)

        LOGGER.info("Writing {} clinical records to database", records.size)
        access.writeClinicalRecords(records)
    }

    companion object {
        const val APPLICATION = "ACTIN Shared Data Loader"

        val LOGGER: Logger = LogManager.getLogger(SharedDataLoaderApplication::class.java)
        private val VERSION = SharedDataLoaderApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = SharedDataLoaderConfig.createOptions()
    val config: SharedDataLoaderConfig
    try {
        config = SharedDataLoaderConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        SharedDataLoaderApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(SharedDataLoaderApplication.APPLICATION, options)
        exitProcess(1)
    }

    SharedDataLoaderApplication(config).run()
}