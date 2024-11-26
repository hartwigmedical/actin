package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

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

    fun read(clinicalFeedDirectory: String): EmcClinicalFeed {
        LOGGER.info("Reading clinical feed data from {}", clinicalFeedDirectory)
        val basePath = Paths.forceTrailingFileSeparator(clinicalFeedDirectory)
        val feed = listOf(
            FeedExtraction(PATIENT_TSV, FeedFileReaderFactory.createPatientReader()),
            FeedExtraction(DIGITAL_FILE_TSV, FeedFileReaderFactory.createDigitalFileReader()),
            FeedExtraction(QUESTIONNAIRE_TSV, FeedFileReaderFactory.createQuestionnaireReader()),
            FeedExtraction(SURGERY_TSV, FeedFileReaderFactory.createSurgeryReader()),
            FeedExtraction(MEDICATION_TSV, FeedFileReaderFactory.createMedicationReader()),
            FeedExtraction(LAB_TSV, FeedFileReaderFactory.createLabReader()),
            FeedExtraction(VITAL_FUNCTION_TSV, FeedFileReaderFactory.createVitalFunctionReader()),
            FeedExtraction(INTOLERANCE_TSV, FeedFileReaderFactory.createIntoleranceReader()),
            FeedExtraction(BODY_WEIGHT_TSV, FeedFileReaderFactory.createBodyWeightReader())
        ).map { readEntriesFromFile(basePath, it) }.fold(EmcClinicalFeed()) { acc, feed -> acc + feed }
        ClinicalFeedValidation.validate(feed)
        return feed
    }

    private fun <T : FeedEntry> readEntriesFromFile(
        basePath: String, feedExtraction: FeedExtraction<T>
    ) = feedExtraction.feedFileReader.read(basePath + feedExtraction.tsv)
}

data class FeedExtraction<T : FeedEntry>(
    val tsv: String,
    val feedFileReader: FeedFileReader<T>
)