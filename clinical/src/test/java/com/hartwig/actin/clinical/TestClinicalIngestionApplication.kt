package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationModel
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.feed.FeedModel.Companion.fromFeedDirectory
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException

class TestClinicalIngestionApplication private constructor(private val config: ClinicalIngestionConfig) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading DOID tree from {}", config.doidJson())
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson())
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
        LOGGER.info("Creating clinical feed model from directory {}", config.feedDirectory())
        val feedModel = fromFeedDirectory(config.feedDirectory())
        LOGGER.info("Creating clinical curation model from directory {}", config.curationDirectory())
        val curationModel = CurationModel.create(config.curationDirectory(), DoidModelFactory.createFromDoidEntry(doidEntry))
        val records: List<ClinicalRecord> = ClinicalRecordsFactory(feedModel, curationModel).create()
        val outputDirectory = config.outputDirectory()
        LOGGER.info("Writing {} clinical records to {}", records.size, outputDirectory)
        ClinicalRecordJson.write(records, outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            TestClinicalIngestionApplication::class.java
        )
        private const val APPLICATION = "ACTIN Clinical Ingestion"
        private val VERSION = TestClinicalIngestionApplication::class.java.getPackage().implementationVersion
        private val FEED_DIRECTORY_PATH =
            java.lang.String.join(File.separator, java.util.List.of(System.getProperty("user.home"), "hmf", "tmp", "feed"))
        private val CURATION_DIRECTORY_PATH = java.lang.String.join(
            File.separator,
            java.util.List.of(System.getProperty("user.home"), "hmf", "repos", "crunch-resources-private", "actin", "clinical_curation")
        )
        private val DOID_JSON_PATH = java.lang.String.join(
            File.separator,
            java.util.List.of(System.getProperty("user.home"), "hmf", "repos", "common-resources-public", "disease_ontology", "doid.json")
        )
        private val OUTPUT_DIRECTORY_PATH = java.lang.String.join(File.separator, java.util.List.of(FEED_DIRECTORY_PATH, "out"))

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val config: ClinicalIngestionConfig = ImmutableClinicalIngestionConfig.builder()
                .feedDirectory(FEED_DIRECTORY_PATH)
                .curationDirectory(CURATION_DIRECTORY_PATH)
                .doidJson(DOID_JSON_PATH)
                .outputDirectory(OUTPUT_DIRECTORY_PATH)
                .build()
            TestClinicalIngestionApplication(config).run()
        }
    }
}