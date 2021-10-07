package com.hartwig.actin.clinical.feed;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.util.FileUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ClinicalFeedReader {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalFeedReader.class);

    private static final String PATIENT_TSV = "patient.tsv";
    private static final String QUESTIONNAIRE_TSV = "questionnaire.tsv";
    private static final String ENCOUNTER_TSV = "encounter.tsv";
    private static final String MEDICATION_TSV = "medication.tsv";
    private static final String LAB_TSV = "lab.tsv";
    private static final String BLOOD_PRESSURE_TSV = "bloodpressure.tsv";
    private static final String COMPLICATION_TSV = "complication.tsv";
    private static final String INTOLERANCE_TSV = "intolerance.tsv";

    private ClinicalFeedReader() {
    }

    @NotNull
    public static ClinicalFeed read(@NotNull String clinicalFeedDirectory) throws IOException {
        LOGGER.info("Reading clinical feed data from {}", clinicalFeedDirectory);

        String basePath = FileUtil.appendFileSeparator(clinicalFeedDirectory);
        ClinicalFeed feed = ImmutableClinicalFeed.builder()
                .patientEntries(readPatientEntries(basePath + PATIENT_TSV))
                .questionnaireEntries(readQuestionnaireEntries(basePath + QUESTIONNAIRE_TSV))
                .encounterEntries(readEncounterEntries(basePath + ENCOUNTER_TSV))
                .medicationEntries(readMedicationEntries(basePath + MEDICATION_TSV))
                .labEntries(readLabEntries(basePath + LAB_TSV))
                .bloodPressureEntries(readBloodPressureEntries(basePath + BLOOD_PRESSURE_TSV))
                .complicationEntries(readComplicationEntries(basePath + COMPLICATION_TSV))
                .intoleranceEntries(readIntoleranceEntries(basePath + INTOLERANCE_TSV))
                .build();

        ClinicalFeedValidation.validate(feed);

        return feed;
    }

    @NotNull
    private static List<PatientEntry> readPatientEntries(@NotNull String patientTsv) throws IOException {
        List<PatientEntry> entries = FeedFileReaderFactory.createPatientReader().read(patientTsv);
        LOGGER.info(" Read {} patient entries from {}", entries.size(), patientTsv);
        return entries;
    }

    @NotNull
    private static List<QuestionnaireEntry> readQuestionnaireEntries(@NotNull String questionnaireTsv) throws IOException {
        List<QuestionnaireEntry> entries = FeedFileReaderFactory.createQuestionnaireReader().read(questionnaireTsv);
        LOGGER.info(" Read {} questionnaire entries from {}", entries.size(), questionnaireTsv);
        return entries;
    }

    @NotNull
    private static List<EncounterEntry> readEncounterEntries(@NotNull String encounterTsv) throws IOException {
        List<EncounterEntry> entries = FeedFileReaderFactory.createEncounterReader().read(encounterTsv);
        LOGGER.info(" Read {} encounter entries from {}", entries.size(), encounterTsv);
        return entries;
    }

    @NotNull
    private static List<MedicationEntry> readMedicationEntries(@NotNull String medicationTsv) throws IOException {
        List<MedicationEntry> entries = FeedFileReaderFactory.createMedicationReader().read(medicationTsv);
        LOGGER.info(" Read {} medication entries from {}", entries.size(), medicationTsv);
        return entries;
    }

    @NotNull
    private static List<LabEntry> readLabEntries(@NotNull String labTsv) throws IOException {
        List<LabEntry> entries = FeedFileReaderFactory.createLabReader().read(labTsv);
        LOGGER.info(" Read {} lab entries from {}", entries.size(), labTsv);
        return entries;
    }

    @NotNull
    private static List<BloodPressureEntry> readBloodPressureEntries(@NotNull String bloodPressureTsv) throws IOException {
        List<BloodPressureEntry> entries = FeedFileReaderFactory.createBloodPressureReader().read(bloodPressureTsv);
        LOGGER.info(" Read {} blood pressure entries from {}", entries.size(), bloodPressureTsv);
        return entries;
    }

    @NotNull
    private static List<ComplicationEntry> readComplicationEntries(@NotNull String complicationTsv) throws IOException {
        List<ComplicationEntry> entries = FeedFileReaderFactory.createComplicationReader().read(complicationTsv);
        LOGGER.info(" Read {} complication entries from {}", entries.size(), complicationTsv);
        return entries;
    }

    @NotNull
    private static List<IntoleranceEntry> readIntoleranceEntries(@NotNull String intoleranceTsv) throws IOException {
        List<IntoleranceEntry> entries = FeedFileReaderFactory.createIntoleranceReader().read(intoleranceTsv);
        LOGGER.info(" Read {} intolerance entries from {}", entries.size(), intoleranceTsv);
        return entries;
    }
}