package com.hartwig.actin.clinical.feed

import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import java.io.IOException

object ClinicalFeedReader {
    private val LOGGER = LogManager.getLogger(ClinicalFeedReader::class.java)
    private const val PATIENT_TSV = "patient.tsv"
    private const val DIGITAL_FILE_TSV = "digital_file.tsv"
    private const val QUESTIONNAIRE_TSV = "questionnaire.tsv"
    private const val SURGERY_TSV = "surgery.tsv"
    private const val MEDICATION_TSV = "medication.tsv"
    private const val LAB_TSV = "lab.tsv"
    private const val VITAL_FUNCTION_TSV = "vital_function.tsv"
    private const val INTOLERANCE_TSV = "intolerance.tsv"
    private const val BODY_WEIGHT_TSV = "bodyweight.tsv"

    @Throws(IOException::class)
    fun read(clinicalFeedDirectory: String): ClinicalFeed {
        LOGGER.info("Reading clinical feed data from {}", clinicalFeedDirectory)
        val basePath = Paths.forceTrailingFileSeparator(clinicalFeedDirectory)
        val feed = listOf(
            FeedExtraction(
                PATIENT_TSV,
                FeedFileReaderFactory.createPatientReader()
            ) { ClinicalFeed(patientEntries = entries(it)) },
            FeedExtraction(
                DIGITAL_FILE_TSV,
                FeedFileReaderFactory.createDigitalFileReader()
            ) { ClinicalFeed(digitalFileEntries = entries(it)) },
            FeedExtraction(
                QUESTIONNAIRE_TSV,
                FeedFileReaderFactory.createQuestionnaireReader()
            ) { ClinicalFeed(questionnaireEntries = entries(it)) },
            FeedExtraction(
                SURGERY_TSV,
                FeedFileReaderFactory.createSurgeryReader()
            ) { ClinicalFeed(surgeryEntries = entries(it)) },
            FeedExtraction(
                MEDICATION_TSV,
                FeedFileReaderFactory.createMedicationReader()
            ) { ClinicalFeed(medicationEntries = entries(it)) },
            FeedExtraction(
                LAB_TSV,
                FeedFileReaderFactory.createLabReader()
            ) { ClinicalFeed(labEntries = entries(it)) },
            FeedExtraction(
                VITAL_FUNCTION_TSV,
                FeedFileReaderFactory.createVitalFunctionReader()
            ) { ClinicalFeed(vitalFunctionEntries = entries(it)) },
            FeedExtraction(
                INTOLERANCE_TSV,
                FeedFileReaderFactory.createIntoleranceReader()
            ) { ClinicalFeed(intoleranceEntries = entries(it)) },
            FeedExtraction(
                BODY_WEIGHT_TSV,
                FeedFileReaderFactory.createBodyWeightReader()
            ) { ClinicalFeed(bodyWeightEntries = entries(it)) }
        ).map { readEntriesFromFile(basePath, it) }.fold(ClinicalFeed()) { acc, feed -> acc + feed }
        ClinicalFeedValidation.validate(feed)
        return feed
    }

    private fun <T : FeedEntry> entries(it: List<FeedResult<T>>) =
        it.map { p -> p.entry }

    private fun <T : FeedEntry> readEntriesFromFile(
        basePath: String, feedExtraction: FeedExtraction<T>
    ): ClinicalFeed {
        val filePath = basePath + feedExtraction.tsv
        val entries = feedExtraction.feedFileReader.read(filePath)
        LOGGER.info(" Read {} entries from {}", entries.size, filePath)
        return feedExtraction.feedCreator.invoke(entries.filter { it.validation.valid })
            .copy(validationWarnings = entries.flatMap { it.validation.warnings })
    }
}

data class FeedExtraction<T : FeedEntry>(
    val tsv: String,
    val feedFileReader: FeedFileReader<T>,
    val feedCreator: (List<FeedResult<T>>) -> ClinicalFeed
)