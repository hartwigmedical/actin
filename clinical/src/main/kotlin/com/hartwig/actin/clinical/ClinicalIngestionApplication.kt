package com.hartwig.actin.clinical

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.curation.CurationModel
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

        LOGGER.info("Creating clinical curation model from directory {}", config.curationDirectory)
        val curationModel: CurationModel =
            CurationModel.create(

                config.curationDirectory, DoidModelFactory.createFromDoidEntry(doidEntry),
                treatmentDatabase
            )

        LOGGER.info("Creating ATC model from file {}", config.atcTsv)
        val atcModel = WhoAtcModel.createFromFile(config.atcTsv)

        LOGGER.info("Creating clinical feed model from directory {}", config.feedDirectory)
        val clinicalFeed = ClinicalFeedReader.read(config.feedDirectory, atcModel)
        val feedModel = FeedModel(
            clinicalFeed.copy(
                questionnaireEntries = QuestionnaireCorrection.correctQuestionnaires(
                    clinicalFeed.questionnaireEntries, curationModel.questionnaireRawEntryMapper()
                )
            )
        )

        val results = ClinicalIngestion(feedModel, curationModel, atcModel).run()
        val outputDirectory = config.outputDirectory
        LOGGER.info("Writing {} clinical records to {}", results.size, outputDirectory)
        ClinicalRecordJson.write(results.map { it.clinicalRecord }, outputDirectory)
        LOGGER.info("Done!")

        val resultsJson = Paths.get(outputDirectory).resolve("results.json")
        LOGGER.info("Writing {} ingestion results to {}", results.size, resultsJson)
        Files.write(
            resultsJson,
            GsonSerializer.create().toJson(results).toByteArray()
        )
        LOGGER.warn("Summary of warnings:")
        results.forEach {
            if (it.warnings.isNotEmpty()) {
                LOGGER.warn("Curation warnings for patient ${it.patientId}")
                it.warnings.forEach { warning -> LOGGER.warn(warning) }
            }
        }
        LOGGER.warn("Summary complete.")


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

    ClinicalIngestionApplication(config).run()
}
