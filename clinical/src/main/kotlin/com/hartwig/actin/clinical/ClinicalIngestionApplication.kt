package com.hartwig.actin.clinical

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.curation.CurationModel
import com.hartwig.actin.clinical.feed.ClinicalFeedReader
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
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

        LOGGER.info("ATC model is currently disabled")
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

        val records = ClinicalRecordsFactory(feedModel, curationModel, atcModel).create()
        val outputDirectory = config.outputDirectory
        LOGGER.info("Writing {} clinical records to {}", records.size, outputDirectory)
        ClinicalRecordJson.write(records, outputDirectory)
        LOGGER.info("Done!")
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
