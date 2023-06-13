package com.hartwig.actin.clinical.feed;

import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry;
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntryCreator;
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry;
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntryCreator;
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry;
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntryCreator;
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
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry;
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntryCreator;

import org.jetbrains.annotations.NotNull;

public final class FeedFileReaderFactory {

    private FeedFileReaderFactory() {
    }

    @NotNull
    public static FeedFileReader<PatientEntry> createPatientReader() {
        return FeedFileReader.create(new PatientEntryCreator());
    }

    @NotNull
    public static FeedFileReader<QuestionnaireEntry> createQuestionnaireReader() {
        return new FeedFileReader<>(new QuestionnaireEntryCreator(), true);
    }

    @NotNull
    public static FeedFileReader<DigitalFileEntry> createDigitalFileReader() {
        return FeedFileReader.create(new DigitalFileEntryCreator());
    }

    @NotNull
    public static FeedFileReader<SurgeryEntry> createSurgeryReader() {
        return FeedFileReader.create(new SurgeryEntryCreator());
    }

    @NotNull
    public static FeedFileReader<MedicationEntry> createMedicationReader() {
        return FeedFileReader.create(new MedicationEntryCreator());
    }

    @NotNull
    public static FeedFileReader<LabEntry> createLabReader() {
        return FeedFileReader.create(new LabEntryCreator());
    }

    @NotNull
    public static FeedFileReader<VitalFunctionEntry> createVitalFunctionReader() {
        return FeedFileReader.create(new VitalFunctionEntryCreator());
    }

    @NotNull
    public static FeedFileReader<IntoleranceEntry> createIntoleranceReader() {
        return FeedFileReader.create(new IntoleranceEntryCreator());
    }

    @NotNull
    public static FeedFileReader<BodyWeightEntry> createBodyWeightReader() {
        return FeedFileReader.create(new BodyWeightEntryCreator());
    }
}
