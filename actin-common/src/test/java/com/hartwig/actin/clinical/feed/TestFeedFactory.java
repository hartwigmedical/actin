package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.ImmutableLabEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.ImmutablePatientEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.ImmutableQuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestFeedFactory {

    static final String TEST_SUBJECT = "ACTN-01-02-9999";

    private TestFeedFactory() {
    }

    @NotNull
    public static FeedModel createProperTestFeedModel() {
        return new FeedModel(createTestClinicalFeed());
    }

    @NotNull
    public static FeedModel createMinimalTestFeedModel() {
        return new FeedModel(ImmutableClinicalFeed.builder().patientEntries(createTestPatientEntries()).build());
    }

    @NotNull
    public static ClinicalFeed createTestClinicalFeed() {
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
        return Lists.newArrayList(ImmutablePatientEntry.builder()
                .id("ID")
                .subject(TEST_SUBJECT)
                .birthYear(1960)
                .sex(Sex.MALE)
                .periodStart(LocalDate.of(2021, 6, 1))
                .periodEnd(LocalDate.of(2021, 10, 1))
                .build());
    }

    @NotNull
    private static List<QuestionnaireEntry> createTestQuestionnaireEntries() {
        ImmutableQuestionnaireEntry.Builder baseBuilder =
                ImmutableQuestionnaireEntry.builder().from(TestQuestionnaireFactory.createTestQuestionnaireEntry());

        return Lists.newArrayList(baseBuilder.subject(TEST_SUBJECT).authoredDateTime(LocalDate.of(2021, 7, 1)).build(),
                baseBuilder.subject(TEST_SUBJECT).authoredDateTime(LocalDate.of(2021, 8, 1)).build());
    }

    @NotNull
    private static List<MedicationEntry> createTestMedicationEntries() {
        return Lists.newArrayList();
    }

    @NotNull
    public static List<LabEntry> createTestLabEntries() {
        List<LabEntry> entries = Lists.newArrayList();

        ImmutableLabEntry.Builder baseBuilder = ImmutableLabEntry.builder()
                .subject(TEST_SUBJECT)
                .valueQuantityComparator(Strings.EMPTY)
                .valueString(Strings.EMPTY)
                .codeCode(Strings.EMPTY);

        entries.add(baseBuilder.codeCodeOriginal("LAB1").codeDisplayOriginal("Lab Value 1")
                .issued(LocalDate.of(2018, 5, 29))
                .valueQuantityValue(30D)
                .valueQuantityUnit("U/l")
                .interpretationDisplayOriginal("ok").referenceRangeText("20 - 40").build());

        entries.add(baseBuilder.codeCodeOriginal("LAB2")
                .codeDisplayOriginal("Lab Value 2")
                .issued(LocalDate.of(2018, 5, 29))
                .valueQuantityValue(22D)
                .valueQuantityUnit("mmol/l")
                .interpretationDisplayOriginal("too low")
                .referenceRangeText("> 30")
                .build());

        entries.add(baseBuilder.codeCodeOriginal("LAB3")
                .codeDisplayOriginal("Lab Value 3")
                .issued(LocalDate.of(2018, 5, 29))
                .valueQuantityComparator(">")
                .valueQuantityValue(50D)
                .valueQuantityUnit("mL/min")
                .interpretationDisplayOriginal("ok")
                .referenceRangeText("> 50")
                .build());

        return entries;
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
