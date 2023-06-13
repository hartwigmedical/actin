package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationModel
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException

class ClinicalIngestionApplication private constructor(private val config: ClinicalIngestionConfig) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading DOID tree from {}", config.doidJson())
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson())
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
        LOGGER.info("Creating clinical feed model from directory {}", config.feedDirectory())
        val feedModel: FeedModel = FeedModel.Companion.fromFeedDirectory(config.feedDirectory())
        LOGGER.info("Creating clinical curation model from directory {}", config.curationDirectory())
        val curationModel: CurationModel =
            CurationModel.Companion.create(config.curationDirectory(), DoidModelFactory.createFromDoidEntry(doidEntry))
        val records = ClinicalRecordsFactory(feedModel, curationModel).create()
        val outputDirectory = config.outputDirectory()
        LOGGER.info("Writing {} clinical records to {}", records.size, outputDirectory)
        ClinicalRecordJson.write(records, outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalIngestionApplication::class.java)
        private const val APPLICATION = "ACTIN Clinical Ingestion"
        private val VERSION = ClinicalIngestionApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = ClinicalIngestionConfig.Companion.createOptions()
            var config: ClinicalIngestionConfig? = null
            try {
                config = ClinicalIngestionConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            ClinicalIngestionApplication(config!!).run()
        }
    }
}