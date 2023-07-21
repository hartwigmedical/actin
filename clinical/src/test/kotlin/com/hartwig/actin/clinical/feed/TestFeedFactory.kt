package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.curation.ATC_CODE
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

object TestFeedFactory {
    const val TEST_SUBJECT = "ACTN-01-02-9999"
    fun createProperTestFeedModel(): FeedModel {
        return FeedModel(createTestClinicalFeed())
    }

    fun createMinimalTestFeedModel(): FeedModel {
        return FeedModel(ClinicalFeed(patientEntries = createTestPatientEntries()))
    }

    fun createTestClinicalFeed(): ClinicalFeed {
        return ClinicalFeed(
            patientEntries = createTestPatientEntries(),
            questionnaireEntries = createTestQuestionnaireEntries(),
            digitalFileEntries = createTestDigitalFileEntries(),
            surgeryEntries = createTestSurgeryEntries(),
            medicationEntries = createTestMedicationEntries(),
            labEntries = createTestLabEntries(),
            vitalFunctionEntries = createTestVitalFunctionEntries(),
            intoleranceEntries = createTestIntoleranceEntries(),
            bodyWeightEntries = createTestBodyWeightEntries()
        )
    }

    private fun createTestPatientEntries(): List<PatientEntry> {
        return listOf(
            PatientEntry(
                subject = TEST_SUBJECT,
                birthYear = 1960,
                gender = Gender.MALE,
                periodStart = LocalDate.of(2021, 6, 1),
                periodEnd = LocalDate.of(2021, 10, 1)
            )
        )
    }

    private fun createTestQuestionnaireEntries(): List<QuestionnaireEntry> {
        val questionnaireBase = TestQuestionnaireFactory.createTestQuestionnaireEntry()
        return listOf(
            questionnaireBase.copy(subject = TEST_SUBJECT, authored = LocalDate.of(2021, 7, 1)),
            questionnaireBase.copy(subject = TEST_SUBJECT, authored = LocalDate.of(2021, 8, 1))
        )
    }

    private fun createTestDigitalFileEntries(): List<DigitalFileEntry> {
        val minimalToxicity = minimalDigitalFileEntry(LocalDate.of(2020, 6, 6), "ONC Kuuroverzicht")
        return listOf(
            minimalToxicity.copy(itemText = "Nausea", itemAnswerValueValueString = "2"),
            minimalToxicity.copy(itemText = "Vomiting", itemAnswerValueValueString = Strings.EMPTY),
            minimalToxicity.copy(itemText = "Pain", itemAnswerValueValueString = "0. Not applicable"),
            minimalDigitalFileEntry(LocalDate.of(2020, 7, 7), "Aanvraag bloedproducten_test").copy(itemAnswerValueValueString = "Product")
        )
    }

    private fun minimalDigitalFileEntry(authored: LocalDate, description: String): DigitalFileEntry {
        return DigitalFileEntry(
            subject = TEST_SUBJECT,
            authored = authored,
            description = description,
            itemText = "",
            itemAnswerValueValueString = ""
        )
    }

    private fun createTestSurgeryEntries(): List<SurgeryEntry> {
        return listOf(
            surgeryEntry(LocalDate.of(2015, 10, 10), LocalDate.of(2015, 10, 10)),
            surgeryEntry(LocalDate.of(2015, 10, 10), LocalDate.of(2015, 10, 10))
        )
    }

    private fun surgeryEntry(periodStart: LocalDate, periodEnd: LocalDate): SurgeryEntry {
        return SurgeryEntry(
            subject = TEST_SUBJECT,
            classDisplay = "surgery",
            codeCodingDisplayOriginal = "diagnostics",
            encounterStatus = "planned",
            procedureStatus = "planned",
            periodStart = periodStart,
            periodEnd = periodEnd
        )
    }

    private fun createTestMedicationEntries(): List<MedicationEntry> {
        return listOf(
            medicationEntry(
                status = "active",
                dosageInstruction = "once per day 50-60 mg every month",
                start = LocalDate.of(2019, 2, 2),
                end = LocalDate.of(2019, 4, 4),
                active = true,
                code5ATCCode = ATC_CODE,
                code5ATCDisplay = "PARACETAMOL",
                administrationRoute = "oraal"
            ), medicationEntry(
                status = Strings.EMPTY,
                dosageInstruction = "Irrelevant",
                start = LocalDate.of(2019, 2, 2),
                end = LocalDate.of(2019, 4, 4),
                active = false,
                code5ATCDisplay = Strings.EMPTY
            )
        )
    }

    private fun medicationEntry(
        status: String, dosageInstruction: String, start: LocalDate, end: LocalDate, active: Boolean,
        code5ATCDisplay: String = "",
        administrationRoute: String = "",
        code5ATCCode: String = "",
    ): MedicationEntry {
        return MedicationEntry(
            status = status,
            dosageInstructionText = dosageInstruction,
            periodOfUseValuePeriodStart = start,
            periodOfUseValuePeriodEnd = end,
            active = active,
            code5ATCDisplay = code5ATCDisplay,
            subject = TEST_SUBJECT,
            codeText = Strings.EMPTY,
            code5ATCCode = code5ATCCode,
            chemicalSubgroupDisplay = Strings.EMPTY,
            pharmacologicalSubgroupDisplay = Strings.EMPTY,
            therapeuticSubgroupDisplay = Strings.EMPTY,
            anatomicalMainGroupDisplay = Strings.EMPTY,
            dosageInstructionRouteDisplay = administrationRoute,
            dosageInstructionDoseQuantityUnit = Strings.EMPTY,
            dosageInstructionDoseQuantityValue = 0.0,
            dosageInstructionFrequencyUnit = Strings.EMPTY,
            dosageInstructionFrequencyValue = 0.0,
            dosageInstructionMaxDosePerAdministration = 0.0,
            dosageInstructionPatientInstruction = Strings.EMPTY,
            dosageInstructionAsNeededDisplay = Strings.EMPTY,
            dosageInstructionPeriodBetweenDosagesUnit = Strings.EMPTY,
            dosageInstructionPeriodBetweenDosagesValue = 0.0,
            dosageDoseValue = Strings.EMPTY,
            dosageRateQuantityUnit = Strings.EMPTY,
            dosageDoseUnitDisplayOriginal = Strings.EMPTY,
            stopTypeDisplay = Strings.EMPTY
        )
    }

    fun createTestLabEntries(): List<LabEntry> {
        return listOf(
            LabEntry(
                subject = TEST_SUBJECT,
                valueQuantityComparator = Strings.EMPTY,
                codeCodeOriginal = "LAB1",
                codeDisplayOriginal = "Lab Value 1",
                valueQuantityValue = 30.0,
                valueQuantityUnit = "U/l",
                referenceRangeText = "20 - 40",
                effectiveDateTime = LocalDate.of(2018, 5, 29),
            ), LabEntry(
                subject = TEST_SUBJECT,
                valueQuantityComparator = Strings.EMPTY,
                codeCodeOriginal = "LAB2",
                codeDisplayOriginal = "Lab Value 2",
                valueQuantityValue = 22.0,
                valueQuantityUnit = "mmol/L",
                referenceRangeText = "> 30",
                effectiveDateTime = LocalDate.of(2018, 5, 29),
            ), LabEntry(
                subject = TEST_SUBJECT,
                codeCodeOriginal = "LAB3",
                codeDisplayOriginal = "Lab Value 3",
                valueQuantityComparator = ">",
                valueQuantityValue = 50.0,
                valueQuantityUnit = "mL/min",
                referenceRangeText = "> 50",
                effectiveDateTime = LocalDate.of(2018, 5, 29),
            ), LabEntry(
                subject = TEST_SUBJECT,
                valueQuantityComparator = Strings.EMPTY,
                codeCodeOriginal = "LAB4",
                codeDisplayOriginal = "Lab Value 4",
                valueQuantityValue = 20.0,
                valueQuantityUnit = "mL/min",
                referenceRangeText = Strings.EMPTY,
                effectiveDateTime = LocalDate.of(2018, 5, 29),
            )
        )
    }

    private fun createTestVitalFunctionEntries(): List<VitalFunctionEntry> {
        return listOf(
            VitalFunctionEntry(
                subject = TEST_SUBJECT,
                effectiveDateTime = LocalDate.of(2021, 2, 27),
                codeDisplayOriginal = "NIBP",
                componentCodeDisplay = "systolic",
                quantityUnit = "mm[Hg]",
                quantityValue = 120.0
            )
        )
    }

    private fun createTestIntoleranceEntries(): List<IntoleranceEntry> {
        return listOf(
            IntoleranceEntry(
                subject = TEST_SUBJECT,
                assertedDate = LocalDate.of(2018, 1, 1),
                category = "medication",
                categoryAllergyCategoryDisplay = Strings.EMPTY,
                clinicalStatus = Strings.EMPTY,
                verificationStatus = Strings.EMPTY,
                codeText = "pills",
                criticality = "unknown",
                isSideEffect = "allergy"
            )
        )
    }

    private fun createTestBodyWeightEntries(): List<BodyWeightEntry> {
        return listOf(
            BodyWeightEntry(
                subject = TEST_SUBJECT,
                valueQuantityValue = 58.1,
                valueQuantityUnit = "kilogram",
                effectiveDateTime = LocalDate.of(2018, 4, 5),
            ), BodyWeightEntry(
                subject = TEST_SUBJECT,
                valueQuantityValue = 61.1,
                valueQuantityUnit = "kilogram",
                effectiveDateTime = LocalDate.of(2018, 5, 5),
            ), BodyWeightEntry(
                subject = TEST_SUBJECT,
                valueQuantityValue = 61.1,
                valueQuantityUnit = "kilogram",
                effectiveDateTime = LocalDate.of(2018, 5, 5)
            )
        )
    }
}