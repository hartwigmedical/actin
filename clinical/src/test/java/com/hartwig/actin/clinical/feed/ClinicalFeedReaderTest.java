package com.hartwig.actin.clinical.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalFeedReaderTest {

    private static final String CLINICAL_FEED_DIRECTORY = Resources.getResource("feed").getPath();

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canReadFromTestDirectory() throws IOException {
        ClinicalFeed feed = ClinicalFeedReader.read(CLINICAL_FEED_DIRECTORY);

        assertPatients(feed.patientEntries());
        assertQuestionnaires(feed.questionnaireEntries());
        assertEncounters(feed.encounterEntries());
        assertMedication(feed.medicationEntries());
        assertLab(feed.labEntries());
        assertVitalFunctions(feed.vitalFunctionEntries());
        assertIntolerances(feed.intoleranceEntries());
        assertBodyWeights(feed.bodyWeightEntries());
    }

    private static void assertPatients(@NotNull List<PatientEntry> entries) {
        assertEquals(1, entries.size());

        PatientEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals(1953, entry.birthYear());
        assertEquals(Gender.MALE, entry.gender());
        assertEquals(LocalDate.of(2020, 7, 13), entry.periodStart());
        assertNull(entry.periodEnd());
    }

    private static void assertQuestionnaires(@NotNull List<QuestionnaireEntry> entries) {
        assertEquals(3, entries.size());

        QuestionnaireEntry entry1 = findByParentIdentifierValue(entries, "XX");
        assertEquals("ACTN-01-02-9999", entry1.subject());
        assertEquals(LocalDate.of(2020, 8, 28), entry1.authored());
        assertEquals("A", entry1.questionnaireQuestionnaireValue());
        assertEquals("INT Consult", entry1.description());
        assertEquals("Beloop", entry1.itemText());

        assertEquals(30, entry1.itemAnswerValueValueString().split("\n").length);
        assertTrue(entry1.itemAnswerValueValueString().startsWith("ACTIN Questionnaire"));
        assertTrue(entry1.itemAnswerValueValueString().contains("CNS lesions yes/no/unknown"));
        assertTrue(entry1.itemAnswerValueValueString().contains("Cancer-related complications (e.g. pleural effusion)"));

        QuestionnaireEntry entry2 = findByParentIdentifierValue(entries, "YY");
        assertEquals("ACTN-01-02-9999", entry2.subject());
        assertEquals(LocalDate.of(2021, 6, 6), entry2.authored());
        assertEquals("B", entry2.questionnaireQuestionnaireValue());
        assertEquals("ONC Kuuroverzicht", entry2.description());
        assertEquals("Nausea", entry2.itemText());
        assertEquals("0", entry2.itemAnswerValueValueString());

        QuestionnaireEntry entry3 = findByParentIdentifierValue(entries, "ZZ");
        assertEquals("ACTN-01-02-9999", entry3.subject());
        assertEquals(LocalDate.of(2021, 8, 16), entry3.authored());
        assertEquals("A", entry3.questionnaireQuestionnaireValue());
        assertEquals("INT Consult", entry3.description());
        assertEquals("Beloop", entry3.itemText());
        assertEquals(26, entry3.itemAnswerValueValueString().split("\n").length);
        assertTrue(entry3.itemAnswerValueValueString().startsWith("ACTIN Questionnaire"));
        assertTrue(entry3.itemAnswerValueValueString().contains("CNS lesions yes/no/unknown"));
        assertTrue(entry3.itemAnswerValueValueString().contains("Other (e.g. Osteoporosis, Pleural effusion)"));
    }

    @NotNull
    private static QuestionnaireEntry findByParentIdentifierValue(@NotNull List<QuestionnaireEntry> entries,
            @NotNull String parentIdentifierValue) {
        for (QuestionnaireEntry entry : entries) {
            if (entry.parentIdentifierValue().equals(parentIdentifierValue)) {
                return entry;
            }
        }

        throw new IllegalStateException("No questionnaire entry found with parentIdentifierValue '" + parentIdentifierValue + "'");
    }

    private static void assertEncounters(@NotNull List<EncounterEntry> entries) {
        assertEquals(1, entries.size());

        EncounterEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertTrue(entry.type1Display().isEmpty());
        assertEquals("surgery", entry.classDisplay());
        assertEquals(LocalDate.of(2020, 8, 28), entry.periodStart());
        assertEquals(LocalDate.of(2020, 8, 28), entry.periodEnd());
        assertEquals("ID", entry.identifierValue());
        assertEquals("URL", entry.identifierSystem());
        assertEquals("code", entry.codeCodingCodeOriginal());
        assertEquals("diagnostics stomach", entry.codeCodingDisplayOriginal());
        assertEquals(Strings.EMPTY, entry.reason());
        assertEquals(Strings.EMPTY, entry.accessionValue());
    }

    private static void assertMedication(@NotNull List<MedicationEntry> entries) {
        assertEquals(1, entries.size());

        MedicationEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals("P_90059949", entry.medicationReferenceMedicationValue());
        assertEquals("EPD", entry.medicationReferenceMedicationSystem());
        assertEquals("19-0716 PEMBROLIZUMAB V/P INFOPL 25MG/ML FL 4ML", entry.codeText());
        assertTrue(entry.code5ATCDisplay().isEmpty());
        assertTrue(entry.indicationDisplay().isEmpty());
        assertEquals("MILLIGRAM", entry.dosageInstructionDoseQuantityUnit());
        assertEquals(200, entry.dosageInstructionDoseQuantityValue(), EPSILON);
        assertTrue(entry.dosageInstructionFrequencyUnit().isEmpty());
        assertEquals(0, entry.dosageInstructionFrequencyValue(), EPSILON);
        assertEquals(0, entry.dosageInstructionMaxDosePerAdministration(), EPSILON);
        assertEquals("Excreta: nvt", entry.dosageInstructionPatientInstruction());
        assertTrue(entry.dosageInstructionAsNeededDisplay().isEmpty());
        assertTrue(entry.dosageInstructionPeriodBetweenDosagesUnit().isEmpty());
        assertEquals(0, entry.dosageInstructionPeriodBetweenDosagesValue(), EPSILON);
        assertEquals("200 milligram inlooptijd: 30 minuten, via 0,2 um filter", entry.dosageInstructionText());
        assertTrue(entry.status().isEmpty());
        assertNull(entry.active());
        assertTrue(entry.dosageDoseValue().isEmpty());
        assertTrue(entry.dosageRateQuantityUnit().isEmpty());
        assertTrue(entry.dosageDoseUnitDisplayOriginal().isEmpty());
        assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodStart());
        assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodEnd());
        assertEquals("Definitief", entry.stopTypeDisplay());
        assertEquals("Inpatient", entry.categoryMedicationRequestCategoryDisplay());
        assertEquals("K", entry.categoryMedicationRequestCategoryCodeOriginal());
    }

    private static void assertLab(@NotNull List<LabEntry> entries) {
        assertEquals(2, entries.size());

        LabEntry entry1 = findByCodeCodeOriginal(entries, "HT");
        assertEquals("ACTN-01-02-9999", entry1.subject());
        assertEquals("Hematocriet", entry1.codeDisplayOriginal());
        assertEquals(LocalDate.of(2019, 6, 28), entry1.issued());
        assertEquals(Strings.EMPTY, entry1.valueQuantityComparator());
        assertEquals(0.36, entry1.valueQuantityValue(), EPSILON);
        assertEquals("L/L", entry1.valueQuantityUnit());
        assertEquals("Referentiewaarde \"te laag\" overschreden", entry1.interpretationDisplayOriginal());
        assertEquals(Strings.EMPTY, entry1.valueString());
        assertEquals(Strings.EMPTY, entry1.codeCode());
        assertEquals("0.42 - 0.52", entry1.referenceRangeText());
        assertEquals(LocalDate.of(2019, 6, 27), entry1.effectiveDateTime());

        LabEntry entry2 = findByCodeCodeOriginal(entries, "HB");
        assertEquals("ACTN-01-02-9999", entry2.subject());
        assertEquals("Hemoglobine", entry2.codeDisplayOriginal());
        assertEquals(LocalDate.of(2019, 5, 28), entry2.issued());
        assertEquals(Strings.EMPTY, entry2.valueQuantityComparator());
        assertEquals(4.2, entry2.valueQuantityValue(), EPSILON);
        assertEquals("mmol/L", entry2.valueQuantityUnit());
        assertEquals("Referentiewaarde \"te laag\" overschreden", entry2.interpretationDisplayOriginal());
        assertEquals(Strings.EMPTY, entry2.valueString());
        assertEquals(Strings.EMPTY, entry2.codeCode());
        assertEquals("8.8 - 10.7", entry2.referenceRangeText());
        assertEquals(LocalDate.of(2019, 5, 27), entry2.effectiveDateTime());
    }

    @NotNull
    private static LabEntry findByCodeCodeOriginal(@NotNull List<LabEntry> entries, @NotNull String codeCodeOriginal) {
        for (LabEntry entry : entries) {
            if (entry.codeCodeOriginal().equals(codeCodeOriginal)) {
                return entry;
            }
        }

        throw new IllegalStateException("No lab entry found with codeCodeOriginal '" + codeCodeOriginal + "'");
    }

    private static void assertVitalFunctions(@NotNull List<VitalFunctionEntry> entries) {
        assertEquals(1, entries.size());

        VitalFunctionEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals(LocalDate.of(2019, 4, 28), entry.effectiveDateTime());
        assertEquals("CS00000003", entry.codeCodeOriginal());
        assertEquals("NIBP", entry.codeDisplayOriginal());
        assertEquals("8481-6", entry.componentCodeCode());
        assertEquals("Systolic blood pressure", entry.componentCodeDisplay());
        assertEquals("mm[Hg]", entry.quantityUnit());
        assertEquals(108, entry.quantityValue(), EPSILON);
    }

    private static void assertIntolerances(@NotNull List<IntoleranceEntry> entries) {
        assertEquals(1, entries.size());

        IntoleranceEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals(LocalDate.of(2014, 4, 21), entry.assertedDate());
        assertEquals("medication", entry.category());
        assertEquals("419511003", entry.categoryAllergyCategoryCode());
        assertEquals("Propensity to adverse reactions to drug", entry.categoryAllergyCategoryDisplay());
        assertEquals("active", entry.clinicalStatus());
        assertEquals("confirmed", entry.verificationStatus());
        assertEquals("Actief", entry.clinicalStatusAllergyStatusDisplayNl());
        assertEquals("SIMVASTATINE", entry.codeText());
        assertEquals("low", entry.criticality());
    }

    private static void assertBodyWeights(@NotNull List<BodyWeightEntry> entries) {
        assertEquals(2, entries.size());

        BodyWeightEntry entry1 = findByDate(entries, LocalDate.of(2020, 8, 11));
        assertEquals("ACTN-01-02-9999", entry1.subject());
        assertEquals(61.1, entry1.valueQuantityValue(), EPSILON);
        assertEquals("kilogram", entry1.valueQuantityUnit());

        BodyWeightEntry entry2 = findByDate(entries, LocalDate.of(2020, 8, 20));
        assertEquals("ACTN-01-02-9999", entry2.subject());
        assertEquals(58.9, entry2.valueQuantityValue(), EPSILON);
        assertEquals("kilogram", entry2.valueQuantityUnit());
    }

    @NotNull
    private static BodyWeightEntry findByDate(@NotNull List<BodyWeightEntry> entries, @NotNull LocalDate dateToFind) {
        for (BodyWeightEntry entry : entries) {
            if (entry.effectiveDateTime().equals(dateToFind)) {
                return entry;
            }
        }

        throw new IllegalStateException("Could not find body weight entry with date '" + dateToFind + "'");
    }
}
