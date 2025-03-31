package com.hartwig.actin.clinical

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.feed.emc.EmcClinicalFeedIngestor
import com.hartwig.actin.clinical.feed.standard.PanelGeneList
import com.hartwig.actin.clinical.feed.standard.StandardDataIngestion
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ingestion.IngestionResult
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.serialization.CsvReader
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.util.json.GsonSerializer
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ClinicalIngestionApplication(private val config: ClinicalIngestionConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)

        LOGGER.info("Loading ICD nodes from {}", config.icdTsv)
        val icdNodes = IcdDeserializer.deserialize(CsvReader.readFromFile(config.icdTsv))
        LOGGER.info(" Loaded {} nodes", icdNodes.size)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        LOGGER.info("Creating ATC model from file {}", config.atcTsv)
        val atcModel = WhoAtcModel.createFromFiles(config.atcTsv, config.atcOverridesTsv)

        LOGGER.info("Loading drug interactions database from file {}", config.drugInteractionsTsv)
        val drugInteractionsDatabase = DrugInteractionsDatabase.create(config.drugInteractionsTsv)
        LOGGER.info("Loading QT prolongating drugs database from file {}", config.qtProlongatingTsv)
        val qtProlongatingDatabase = QtProlongatingDatabase.create(config.qtProlongatingTsv)

        LOGGER.info("Creating clinical curation database from directory {}", config.curationDirectory)
        val curationDoidValidator = CurationDoidValidator(DoidModelFactory.createFromDoidEntry(doidEntry))
        val outputDirectory: String = config.outputDirectory
        val curationDatabaseContext =
            CurationDatabaseContext.create(config.curationDirectory, curationDoidValidator, IcdModel.create(icdNodes), treatmentDatabase)
        val validationErrors = curationDatabaseContext.validate()
        if (validationErrors.isNotEmpty()) {
            LOGGER.warn("Curation input had validation errors:")
            for (validationError in validationErrors) {
                LOGGER.warn(" $validationError")
            }
            LOGGER.warn("Please correct all errors before running the ingestion again. Exiting 1")
            writeIngestionResults(outputDirectory, IngestionResult(validationErrors))
            exitProcess(1)
        }

        LOGGER.info("Creating clinical feed model from directory {} of format {}", config.feedDirectory, config.feedFormat)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val clinicalIngestion = if (config.feedFormat == FeedFormat.EMC_TSV)
            EmcClinicalFeedIngestor.create(
                config.feedDirectory,
                config.curationDirectory,
                curationDatabaseContext,
                atcModel,
                drugInteractionsDatabase,
                qtProlongatingDatabase,
                doidModel,
                treatmentDatabase
            ) else StandardDataIngestion.create(
            config.feedDirectory,
            curationDatabaseContext,
            atcModel,
            drugInteractionsDatabase,
            qtProlongatingDatabase,
            doidModel,
            treatmentDatabase,
            PanelGeneList.create(
                config.panelGeneListTsv ?: throw IllegalStateException("Panel gene list is required when running in standard feed format")
            )
        )
        val clinicalIngestionAdapter =
            ClinicalIngestionFeedAdapter(
                clinicalIngestion,
                curationDatabaseContext
            )

        val (ingestionResult, clinicalRecords) = clinicalIngestionAdapter.run()
        LOGGER.info("Writing {} clinical records to {}", clinicalRecords.size, outputDirectory)
        ClinicalRecordJson.write(clinicalRecords, outputDirectory)
        LOGGER.info("Done!")

        writeIngestionResults(outputDirectory, ingestionResult)

        if (ingestionResult.patientResults.any { it.curationResults.isNotEmpty() }) {
            LOGGER.warn("Summary of warnings:")
            ingestionResult.patientResults.forEach {
                if (it.curationResults.isNotEmpty()) {
                    LOGGER.warn("Curation warnings for patient ${it.patientId}")
                    it.curationResults.flatMap { result -> result.requirements }.forEach { requirement ->
                        LOGGER.warn(requirement.message)
                    }
                }
            }
            LOGGER.warn("Summary complete.")
        }
    }

    private fun writeIngestionResults(outputDirectory: String, results: IngestionResult) {
        val resultsJson = Paths.get(outputDirectory).resolve("clinical_ingestion_results.json")
        LOGGER.info("Writing {} ingestion results to {}", results.patientResults.size, resultsJson)
        Files.write(
            resultsJson,
            GsonSerializer.create().toJson(results).toByteArray()
        )
    }

    companion object {
        const val APPLICATION = "ACTIN Clinical Ingestion"

        val LOGGER: Logger = LogManager.getLogger(ClinicalIngestionApplication::class.java)
        private val VERSION = ClinicalIngestionApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = ClinicalIngestionConfig.createOptions()
    val config: ClinicalIngestionConfig

    try {
        config = ClinicalIngestionConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        ClinicalIngestionApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(ClinicalIngestionApplication.APPLICATION, options)
        exitProcess(1)
    }

    try {
        ClinicalIngestionApplication(config).run()
    } catch (e: Exception) {
        ClinicalIngestionApplication.LOGGER.error("${ClinicalIngestionApplication.APPLICATION} failed on an unrecoverable error", e)
        exitProcess(1)
    }
}
