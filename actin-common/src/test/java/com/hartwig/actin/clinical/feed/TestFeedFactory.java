package com.hartwig.actin.clinical.feed;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.jetbrains.annotations.NotNull;

public final class TestFeedFactory {

    private TestFeedFactory() {
    }

    @NotNull
    public static ClinicalFeed createTestFeed() {
        return ImmutableClinicalFeed.builder()
                .patientEntries(createTestPatientEntries())
                .questionnaireEntries(createTestQuestionnaireEntries())
                .medicationEntries(createTestMedicationEntries())
                .labEntries(createTestLabEntries())
                .bloodPressureEntries(createTestBloodPressureEntries())
                .complicationEntries(createTestComplicationEntries())
                .intoleranceEntries(createTestIntoleranceEntries())
                .build();
    }

    @NotNull
    private static List<PatientEntry> createTestPatientEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    private static List<QuestionnaireEntry> createTestQuestionnaireEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    private static List<MedicationEntry> createTestMedicationEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    private static List<LabEntry> createTestLabEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    private static List<BloodPressureEntry> createTestBloodPressureEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    private static List<ComplicationEntry> createTestComplicationEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    private static List<IntoleranceEntry> createTestIntoleranceEntries() {
        return Lists.newArrayList();
    }
}
