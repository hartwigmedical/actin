package com.hartwig.actin.clinical.feed;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureFile;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationFile;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceFile;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.lab.LabFile;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationFile;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.patient.PatientFile;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class FeedFactory {

    private static final Logger LOGGER = LogManager.getLogger(FeedFactory.class);

    private static final String PATIENT_TSV = "patient.tsv";
    private static final String QUESTIONNAIRE_TSV = "questionnaire.tsv";
    private static final String MEDICATION_TSV = "medication.tsv";
    private static final String LAB_TSV = "lab.tsv";
    private static final String BLOOD_PRESSURE_TSV = "bloodpressure.tsv";
    private static final String COMPLICATION_TSV = "complication.tsv";
    private static final String INTOLERANCE_TSV = "intolerance.tsv";

    private FeedFactory() {
    }

    @NotNull
    public static Feed loadFromClinicalDataDirectory(@NotNull String clinicalDataDirectory) throws IOException {
        LOGGER.info("Reading clinical data from {}", clinicalDataDirectory);

        String basePath = clinicalDataDirectory.endsWith(File.separator) ? clinicalDataDirectory : clinicalDataDirectory + File.separator;
        return ImmutableFeed.builder()
                .patientEntries(readPatientEntries(basePath + PATIENT_TSV))
                .questionnaireEntries(readQuestionnaireEntries(basePath + QUESTIONNAIRE_TSV))
                .medicationEntries(readMedicationEntries(basePath + MEDICATION_TSV))
                .labEntries(readLabEntries(basePath + LAB_TSV))
                .bloodPressureEntries(readBloodPressureEntries(basePath + BLOOD_PRESSURE_TSV))
                .complicationEntries(readComplicationEntries(basePath + COMPLICATION_TSV))
                .intoleranceEntries(readIntoleranceEntries(basePath + INTOLERANCE_TSV))
                .build();
    }

    @NotNull
    private static List<PatientEntry> readPatientEntries(@NotNull String patientTsv) throws IOException {
        List<PatientEntry> entries = PatientFile.read(patientTsv);
        LOGGER.info(" Read {} patient entries from {}", entries.size(), patientTsv);
        return entries;
    }

    @NotNull
    private static List<QuestionnaireEntry> readQuestionnaireEntries(@NotNull String questionnaireTsv) throws IOException {
        List<QuestionnaireEntry> entries = QuestionnaireFile.read(questionnaireTsv);
        LOGGER.info(" Read {} questionnaire entries from {}", entries.size(), questionnaireTsv);
        return entries;
    }

    @NotNull
    private static List<MedicationEntry> readMedicationEntries(@NotNull String medicationTsv) throws IOException {
        List<MedicationEntry> entries = MedicationFile.read(medicationTsv);
        LOGGER.info(" Read {} medication entries from {}", entries.size(), medicationTsv);
        return entries;
    }

    @NotNull
    private static List<LabEntry> readLabEntries(@NotNull String labTsv) throws IOException {
        List<LabEntry> entries = LabFile.read(labTsv);
        LOGGER.info(" Read {} lab entries from {}", entries.size(), labTsv);
        return entries;
    }

    @NotNull
    private static List<BloodPressureEntry> readBloodPressureEntries(@NotNull String bloodPressureTsv) throws IOException {
        List<BloodPressureEntry> entries = BloodPressureFile.read(bloodPressureTsv);
        LOGGER.info(" Read {} blood pressure entries from {}", entries.size(), bloodPressureTsv);
        return entries;
    }

    @NotNull
    private static List<ComplicationEntry> readComplicationEntries(@NotNull String complicationTsv) throws IOException {
        List<ComplicationEntry> entries = ComplicationFile.read(complicationTsv);
        LOGGER.info(" Read {} complication entries from {}", entries.size(), complicationTsv);
        return entries;
    }

    @NotNull
    private static List<IntoleranceEntry> readIntoleranceEntries(@NotNull String intoleranceTsv) throws IOException {
        List<IntoleranceEntry> entries = IntoleranceFile.read(intoleranceTsv);
        LOGGER.info(" Read {} intolerance entries from {}", entries.size(), intoleranceTsv);
        return entries;
    }
}
