package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.encounter.ImmutableEncounterEntry;
import com.hartwig.actin.clinical.feed.intolerance.ImmutableIntoleranceEntry;
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
                .encounterEntries(createTestEncounterEntries())
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
        ImmutableQuestionnaireEntry.Builder questionnaireBuilder =
                ImmutableQuestionnaireEntry.builder().from(TestQuestionnaireFactory.createTestQuestionnaireEntry());

        List<QuestionnaireEntry> entries = Lists.newArrayList();
        entries.add(questionnaireBuilder.subject(TEST_SUBJECT).authoredDateTime(LocalDate.of(2021, 7, 1)).build());
        entries.add(questionnaireBuilder.subject(TEST_SUBJECT).authoredDateTime(LocalDate.of(2021, 8, 1)).build());

        ImmutableQuestionnaireEntry.Builder toxicityBuilder = ImmutableQuestionnaireEntry.builder()
                .subject(TEST_SUBJECT)
                .parentIdentifierValue(Strings.EMPTY)
                .authoredDateTime(LocalDate.of(2020, 6, 6))
                .questionnaireQuestionnaireValue(Strings.EMPTY)
                .description("ONC Kuuroverzicht");

        entries.add(toxicityBuilder.itemText("Nausea").itemAnswerValueValueString("2").build());
        entries.add(toxicityBuilder.itemText("Vomiting").itemAnswerValueValueString(Strings.EMPTY).build());

        return entries;
    }

    @NotNull
    private static List<EncounterEntry> createTestEncounterEntries() {
        List<EncounterEntry> entries = Lists.newArrayList();

        entries.add(ImmutableEncounterEntry.builder()
                .subject(TEST_SUBJECT)
                .type1Display(Strings.EMPTY)
                .classDisplay("surgery")
                .periodStart(LocalDate.of(2015, 10, 10))
                .periodEnd(LocalDate.of(2015, 10, 10))
                .identifierValue("ID")
                .identifierSystem("URL")
                .codeCodingCodeOriginal("code")
                .codeCodingDisplayOriginal("diagnostics")
                .presentedFormData(Strings.EMPTY)
                .reason(Strings.EMPTY)
                .accessionValue(Strings.EMPTY)
                .build());

        return entries;
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

        entries.add(baseBuilder.codeCodeOriginal("LAB1")
                .codeDisplayOriginal("Lab Value 1")
                .issued(LocalDate.of(2018, 5, 29))
                .valueQuantityValue(30D)
                .valueQuantityUnit("U/l")
                .interpretationDisplayOriginal("ok")
                .referenceRangeText("20 - 40")
                .build());

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

        entries.add(baseBuilder.codeCodeOriginal("LAB4")
                .codeDisplayOriginal("Lab Value 4")
                .issued(LocalDate.of(2018, 5, 29))
                .valueQuantityValue(20D)
                .valueQuantityUnit("mL/min")
                .interpretationDisplayOriginal("ok")
                .referenceRangeText(Strings.EMPTY)
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
        List<IntoleranceEntry> entries = Lists.newArrayList();

        entries.add(ImmutableIntoleranceEntry.builder()
                .subject(TEST_SUBJECT)
                .assertedDate(LocalDate.of(2018, 1, 1))
                .category("medication")
                .categoryAllergyCategoryCode(Strings.EMPTY)
                .categoryAllergyCategoryDisplay(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .codeText("pills")
                .criticality("unknown")
                .build());

        return entries;
    }
}
