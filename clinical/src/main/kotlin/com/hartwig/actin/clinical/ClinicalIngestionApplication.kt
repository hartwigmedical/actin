package com.hartwig.actin.clinical

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.feed.ClinicalFeedReader
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class ClinicalIngestionApplication(private val config: ClinicalIngestionConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        LOGGER.info("Creating ATC model from file {}", config.atcTsv)
        val atcModel = WhoAtcModel.createFromFile(config.atcTsv)

        LOGGER.info("Creating clinical feed model from directory {}", config.feedDirectory)
        val clinicalFeed = ClinicalFeedReader.read(config.feedDirectory)
        val feedModel = FeedModel(
            clinicalFeed.copy(
                questionnaireEntries = QuestionnaireCorrection.correctQuestionnaires(
                    clinicalFeed.questionnaireEntries, QuestionnaireRawEntryMapper.createFromCurationDirectory(config.curationDirectory)
                )
            )
        )

        LOGGER.info("Creating clinical curation database from directory {}", config.curationDirectory)
        val curationDoidValidator = CurationDoidValidator(DoidModelFactory.createFromDoidEntry(doidEntry))
        val outputDirectory: String = config.outputDirectory
        val curationDatabaseContext = CurationDatabaseContext.create(config.curationDirectory, curationDoidValidator, treatmentDatabase)
        val validationErrors = curationDatabaseContext.validate()
        if (validationErrors.isNotEmpty()) {
            LOGGER.warn("Curation input had validation errors. Writing to validation errors json and exiting 1")
            writeIngestionResults(outputDirectory, IngestionResult(validationErrors))
            exitProcess(1)
        }

        val clinicalIngestion =
            ClinicalIngestion.create(
                feedModel,
                curationDatabaseContext,
                atcModel
            )

        val ingestionResult = clinicalIngestion.run()
        LOGGER.info("Writing {} clinical records to {}", ingestionResult.patientResults.size, outputDirectory)
        ClinicalRecordJson.write(ingestionResult.patientResults.map { it.clinicalRecord }, outputDirectory)
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
        val LOGGER: Logger = LogManager.getLogger(ClinicalIngestionApplication::class.java)
        const val APPLICATION = "ACTIN Clinical Ingestion"
        private val VERSION = ClinicalIngestionApplication::class.java.getPackage().implementationVersion
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
