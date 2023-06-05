package com.hartwig.actin.clinical.feed;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ClinicalFeedReader {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalFeedReader.class);

    private static final String PATIENT_TSV = "patient.tsv";
    private static final String DIGITAL_FILE_TSV = "digital_file.tsv";
    private static final String QUESTIONNAIRE_TSV = "questionnaire.tsv";
    private static final String SURGERY_TSV = "surgery.tsv";
    private static final String MEDICATION_TSV = "medication.tsv";
    private static final String LAB_TSV = "lab.tsv";
    private static final String VITAL_FUNCTION_TSV = "vital_function.tsv";
    private static final String INTOLERANCE_TSV = "intolerance.tsv";
    private static final String BODY_WEIGHT_TSV = "bodyweight.tsv";

    private ClinicalFeedReader() {
    }

    @NotNull
    public static ClinicalFeed read(@NotNull String clinicalFeedDirectory) throws IOException {
        LOGGER.info("Reading clinical feed data from {}", clinicalFeedDirectory);

        String basePath = Paths.forceTrailingFileSeparator(clinicalFeedDirectory);
        ClinicalFeed feed = ImmutableClinicalFeed.builder()
                .patientEntries(readEntriesFromFile(basePath, PATIENT_TSV, FeedFileReaderFactory.createPatientReader()))
                .questionnaireEntries(readEntriesFromFile(basePath, QUESTIONNAIRE_TSV, FeedFileReaderFactory.createQuestionnaireReader()))
                .digitalFileEntries(readEntriesFromFile(basePath, DIGITAL_FILE_TSV, FeedFileReaderFactory.createDigitalFileReader()))
                .surgeryEntries(readEntriesFromFile(basePath, SURGERY_TSV, FeedFileReaderFactory.createSurgeryReader()))
                .medicationEntries(readEntriesFromFile(basePath, MEDICATION_TSV, FeedFileReaderFactory.createMedicationReader()))
                .labEntries(readEntriesFromFile(basePath, LAB_TSV, FeedFileReaderFactory.createLabReader()))
                .vitalFunctionEntries(readEntriesFromFile(basePath, VITAL_FUNCTION_TSV, FeedFileReaderFactory.createVitalFunctionReader()))
                .intoleranceEntries(readEntriesFromFile(basePath, INTOLERANCE_TSV, FeedFileReaderFactory.createIntoleranceReader()))
                .bodyWeightEntries(readEntriesFromFile(basePath, BODY_WEIGHT_TSV, FeedFileReaderFactory.createBodyWeightReader()))
                .build();

        ClinicalFeedValidation.validate(feed);

        return feed;
    }

    @NotNull
    private static <T extends FeedEntry> List<T> readEntriesFromFile(@NotNull String basePath, @NotNull String fileName,
            @NotNull FeedFileReader<T> fileReader) throws IOException {
        String filePath = basePath + fileName;
        List<T> entries = fileReader.read(filePath);
        LOGGER.info(" Read {} entries from {}", entries.size(), filePath);
        return entries;
    }
}
