package com.hartwig.actin.clinical.feed;

import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntryCreator;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntryCreator;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntryCreator;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntryCreator;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntryCreator;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntryCreator;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntryCreator;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntryCreator;

import org.jetbrains.annotations.NotNull;

public final class FeedFileReaderFactory {

    private FeedFileReaderFactory() {
    }

    @NotNull
    public static FeedFileReader<PatientEntry> createPatientReader() {
        return new FeedFileReader<>(new PatientEntryCreator());
    }

    @NotNull
    public static FeedFileReader<QuestionnaireEntry> createQuestionnaireReader() {
        return new FeedFileReader<>(new QuestionnaireEntryCreator());
    }

    @NotNull
    public static FeedFileReader<EncounterEntry> createEncounterReader() {
        return new FeedFileReader<>(new EncounterEntryCreator());
    }

    @NotNull
    public static FeedFileReader<MedicationEntry> createMedicationReader() {
        return new FeedFileReader<>(new MedicationEntryCreator());
    }

    @NotNull
    public static FeedFileReader<LabEntry> createLabReader() {
        return new FeedFileReader<>(new LabEntryCreator());
    }

    @NotNull
    public static FeedFileReader<BloodPressureEntry> createBloodPressureReader() {
        return new FeedFileReader<>(new BloodPressureEntryCreator());
    }

    @NotNull
    public static FeedFileReader<ComplicationEntry> createComplicationReader() {
        return new FeedFileReader<>(new ComplicationEntryCreator());
    }

    @NotNull
    public static FeedFileReader<IntoleranceEntry> createIntoleranceReader() {
        return new FeedFileReader<>(new IntoleranceEntryCreator());
    }
}
