package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.AtcModel
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
    fun read(clinicalFeedDirectory: String, atcModel: AtcModel): ClinicalFeed {
        LOGGER.info("Reading clinical feed data from {}", clinicalFeedDirectory)
        val basePath = Paths.forceTrailingFileSeparator(clinicalFeedDirectory)
        val feed = ClinicalFeed(
            patientEntries = readEntriesFromFile(basePath, PATIENT_TSV, FeedFileReaderFactory.createPatientReader()),
            questionnaireEntries = readEntriesFromFile(basePath, QUESTIONNAIRE_TSV, FeedFileReaderFactory.createQuestionnaireReader()),
            digitalFileEntries = readEntriesFromFile(basePath, DIGITAL_FILE_TSV, FeedFileReaderFactory.createDigitalFileReader()),
            surgeryEntries = readEntriesFromFile(basePath, SURGERY_TSV, FeedFileReaderFactory.createSurgeryReader()),
            medicationEntries = readEntriesFromFile(basePath, MEDICATION_TSV, FeedFileReaderFactory.createMedicationReader(atcModel)),
            labEntries = readEntriesFromFile(basePath, LAB_TSV, FeedFileReaderFactory.createLabReader()),
            vitalFunctionEntries = readEntriesFromFile(basePath, VITAL_FUNCTION_TSV, FeedFileReaderFactory.createVitalFunctionReader()),
            intoleranceEntries = readEntriesFromFile(basePath, INTOLERANCE_TSV, FeedFileReaderFactory.createIntoleranceReader()),
            bodyWeightEntries = readEntriesFromFile(basePath, BODY_WEIGHT_TSV, FeedFileReaderFactory.createBodyWeightReader()),
        )
        ClinicalFeedValidation.validate(feed)
        return feed
    }

    @Throws(IOException::class)
    private fun <T : FeedEntry> readEntriesFromFile(
        basePath: String, fileName: String,
        fileReader: FeedFileReader<T>
    ): List<T> {
        val filePath = basePath + fileName
        val entries = fileReader.read(filePath)
        LOGGER.info(" Read {} entries from {}", entries.size, filePath)
        return entries
    }
}