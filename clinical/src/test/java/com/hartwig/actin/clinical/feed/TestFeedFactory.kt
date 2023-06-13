package com.hartwig.actin.clinical.feed

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.bodyweight.ImmutableBodyWeightEntry
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.digitalfile.ImmutableDigitalFileEntry
import com.hartwig.actin.clinical.feed.intolerance.ImmutableIntoleranceEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.ImmutableLabEntry
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.medication.ImmutableMedicationEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.patient.ImmutablePatientEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.ImmutableQuestionnaireEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory
import com.hartwig.actin.clinical.feed.surgery.ImmutableSurgeryEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.vitalfunction.ImmutableVitalFunctionEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

object TestFeedFactory {
    const val TEST_SUBJECT = "ACTN-01-02-9999"
    fun createProperTestFeedModel(): FeedModel {
        return FeedModel(createTestClinicalFeed())
    }

    fun createMinimalTestFeedModel(): FeedModel {
        return FeedModel(ImmutableClinicalFeed.builder().patientEntries(createTestPatientEntries()).build())
    }

    fun createTestClinicalFeed(): ClinicalFeed {
        return ImmutableClinicalFeed.builder()
            .patientEntries(createTestPatientEntries())
            .questionnaireEntries(createTestQuestionnaireEntries())
            .digitalFileEntries(createTestDigitalFileEntries())
            .surgeryEntries(createTestSurgeryEntries())
            .medicationEntries(createTestMedicationEntries())
            .labEntries(createTestLabEntries())
            .vitalFunctionEntries(createTestVitalFunctionEntries())
            .intoleranceEntries(createTestIntoleranceEntries())
            .bodyWeightEntries(createTestBodyWeightEntries())
            .build()
    }

    private fun createTestPatientEntries(): List<PatientEntry> {
        return Lists.newArrayList<PatientEntry>(
            ImmutablePatientEntry.builder()
                .subject(TEST_SUBJECT)
                .birthYear(1960)
                .gender(Gender.MALE)
                .periodStart(LocalDate.of(2021, 6, 1))
                .periodEnd(LocalDate.of(2021, 10, 1))
                .build()
        )
    }

    private fun createTestQuestionnaireEntries(): List<QuestionnaireEntry> {
        val questionnaireBuilder = ImmutableQuestionnaireEntry.builder().from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
        return java.util.List.of<QuestionnaireEntry>(
            questionnaireBuilder.subject(TEST_SUBJECT).authored(LocalDate.of(2021, 7, 1)).build(),
            questionnaireBuilder.subject(TEST_SUBJECT).authored(LocalDate.of(2021, 8, 1)).build()
        )
    }

    private fun createTestDigitalFileEntries(): List<DigitalFileEntry> {
        val toxicityBuilder = ImmutableDigitalFileEntry.builder()
            .subject(TEST_SUBJECT)
            .authored(LocalDate.of(2020, 6, 6))
            .description("ONC Kuuroverzicht")
        val bloodTransfusionBuilder = ImmutableDigitalFileEntry.builder()
            .subject(TEST_SUBJECT)
            .authored(LocalDate.of(2020, 7, 7))
            .description("Aanvraag bloedproducten_test")
            .itemText(Strings.EMPTY)
        return java.util.List.of<DigitalFileEntry>(
            toxicityBuilder.itemText("Nausea").itemAnswerValueValueString("2").build(),
            toxicityBuilder.itemText("Vomiting").itemAnswerValueValueString(Strings.EMPTY).build(),
            toxicityBuilder.itemText("Pain").itemAnswerValueValueString("0. Not applicable").build(),
            bloodTransfusionBuilder.itemAnswerValueValueString("Product").build()
        )
    }

    private fun createTestSurgeryEntries(): List<SurgeryEntry> {
        val entries: MutableList<SurgeryEntry> = Lists.newArrayList()
        val builder = ImmutableSurgeryEntry.builder()
            .subject(TEST_SUBJECT)
            .classDisplay("surgery")
            .codeCodingDisplayOriginal("diagnostics")
            .encounterStatus("planned")
            .procedureStatus("planned")
        entries.add(builder.periodStart(LocalDate.of(2015, 10, 10)).periodEnd(LocalDate.of(2015, 10, 10)).build())
        entries.add(builder.periodStart(LocalDate.of(2015, 10, 10)).periodEnd(LocalDate.of(2015, 10, 10)).build())
        return entries
    }

    private fun createTestMedicationEntries(): List<MedicationEntry> {
        val entries: MutableList<MedicationEntry> = Lists.newArrayList()
        val builder = ImmutableMedicationEntry.builder()
            .subject(TEST_SUBJECT)
            .codeText(Strings.EMPTY)
            .code5ATCCode(Strings.EMPTY)
            .chemicalSubgroupDisplay(Strings.EMPTY)
            .pharmacologicalSubgroupDisplay(Strings.EMPTY)
            .therapeuticSubgroupDisplay(Strings.EMPTY)
            .anatomicalMainGroupDisplay(Strings.EMPTY)
            .dosageInstructionRouteDisplay(Strings.EMPTY)
            .dosageInstructionDoseQuantityUnit(Strings.EMPTY)
            .dosageInstructionDoseQuantityValue(0.0)
            .dosageInstructionFrequencyUnit(Strings.EMPTY)
            .dosageInstructionFrequencyValue(0.0)
            .dosageInstructionMaxDosePerAdministration(0.0)
            .dosageInstructionPatientInstruction(Strings.EMPTY)
            .dosageInstructionAsNeededDisplay(Strings.EMPTY)
            .dosageInstructionPeriodBetweenDosagesUnit(Strings.EMPTY)
            .dosageInstructionPeriodBetweenDosagesValue(0.0)
            .dosageDoseValue(Strings.EMPTY)
            .dosageRateQuantityUnit(Strings.EMPTY)
            .dosageDoseUnitDisplayOriginal(Strings.EMPTY)
            .stopTypeDisplay(Strings.EMPTY)
        entries.add(
            builder.code5ATCDisplay("PARACETAMOL")
                .status("active")
                .dosageInstructionText("50-60 mg per day")
                .periodOfUseValuePeriodStart(LocalDate.of(2019, 2, 2))
                .periodOfUseValuePeriodEnd(LocalDate.of(2019, 4, 4))
                .active(true)
                .build()
        )
        entries.add(
            builder.code5ATCDisplay(Strings.EMPTY)
                .status(Strings.EMPTY)
                .dosageInstructionText("Irrelevant")
                .periodOfUseValuePeriodStart(LocalDate.of(2019, 2, 2))
                .periodOfUseValuePeriodEnd(LocalDate.of(2019, 4, 4))
                .active(false)
                .build()
        )
        return entries
    }

    fun createTestLabEntries(): List<LabEntry> {
        val entries: MutableList<LabEntry> = Lists.newArrayList()
        val baseBuilder = ImmutableLabEntry.builder().subject(TEST_SUBJECT).valueQuantityComparator(Strings.EMPTY)
        entries.add(
            baseBuilder.codeCodeOriginal("LAB1")
                .codeDisplayOriginal("Lab Value 1")
                .valueQuantityValue(30.0)
                .valueQuantityUnit("U/l")
                .referenceRangeText("20 - 40")
                .effectiveDateTime(LocalDate.of(2018, 5, 29))
                .build()
        )
        entries.add(
            baseBuilder.codeCodeOriginal("LAB2")
                .codeDisplayOriginal("Lab Value 2")
                .valueQuantityValue(22.0)
                .valueQuantityUnit("mmol/L")
                .referenceRangeText("> 30")
                .effectiveDateTime(LocalDate.of(2018, 5, 29))
                .build()
        )
        entries.add(
            baseBuilder.codeCodeOriginal("LAB3")
                .codeDisplayOriginal("Lab Value 3")
                .valueQuantityComparator(">")
                .valueQuantityValue(50.0)
                .valueQuantityUnit("mL/min")
                .referenceRangeText("> 50")
                .effectiveDateTime(LocalDate.of(2018, 5, 29))
                .build()
        )
        entries.add(
            baseBuilder.codeCodeOriginal("LAB4")
                .codeDisplayOriginal("Lab Value 4")
                .valueQuantityValue(20.0)
                .valueQuantityUnit("mL/min")
                .referenceRangeText(Strings.EMPTY)
                .effectiveDateTime(LocalDate.of(2018, 5, 29))
                .build()
        )
        return entries
    }

    private fun createTestVitalFunctionEntries(): List<VitalFunctionEntry> {
        val entries: MutableList<VitalFunctionEntry> = Lists.newArrayList()
        entries.add(
            ImmutableVitalFunctionEntry.builder()
                .subject(TEST_SUBJECT)
                .effectiveDateTime(LocalDate.of(2021, 2, 27))
                .codeDisplayOriginal("NIBP")
                .componentCodeDisplay("systolic")
                .quantityUnit("mm[Hg]")
                .quantityValue(120.0)
                .build()
        )
        return entries
    }

    private fun createTestIntoleranceEntries(): List<IntoleranceEntry> {
        val entries: MutableList<IntoleranceEntry> = Lists.newArrayList()
        entries.add(
            ImmutableIntoleranceEntry.builder()
                .subject(TEST_SUBJECT)
                .assertedDate(LocalDate.of(2018, 1, 1))
                .category("medication")
                .categoryAllergyCategoryDisplay(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .codeText("pills")
                .criticality("unknown")
                .isSideEffect("allergy")
                .build()
        )
        return entries
    }

    private fun createTestBodyWeightEntries(): List<BodyWeightEntry> {
        val entries: MutableList<BodyWeightEntry> = Lists.newArrayList()
        entries.add(
            ImmutableBodyWeightEntry.builder()
                .subject(TEST_SUBJECT)
                .valueQuantityValue(58.1)
                .valueQuantityUnit("kilogram")
                .effectiveDateTime(LocalDate.of(2018, 4, 5))
                .build()
        )
        entries.add(
            ImmutableBodyWeightEntry.builder()
                .subject(TEST_SUBJECT)
                .valueQuantityValue(61.1)
                .valueQuantityUnit("kilogram")
                .effectiveDateTime(LocalDate.of(2018, 5, 5))
                .build()
        )
        entries.add(
            ImmutableBodyWeightEntry.builder()
                .subject(TEST_SUBJECT)
                .valueQuantityValue(61.1)
                .valueQuantityUnit("kilogram")
                .effectiveDateTime(LocalDate.of(2018, 5, 5))
                .build()
        )
        return entries
    }
}