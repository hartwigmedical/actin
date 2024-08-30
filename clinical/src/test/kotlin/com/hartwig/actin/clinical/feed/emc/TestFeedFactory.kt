package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.curation.FULL_ATC_CODE
import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.TestQuestionnaireFactory
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import java.time.LocalDate
import java.time.LocalDateTime

private const val TEST_SUBJECT = "ACTN-01-02-9999"
val FEED_DIRECTORY = resourceOnClasspath("feed/emc") + "/"

object TestFeedFactory {

    fun createProperTestFeedModel(): FeedModel {
        return FeedModel(createTestClinicalFeed())
    }

    fun createTestClinicalFeed(): EmcClinicalFeed {
        return EmcClinicalFeed(
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
            minimalToxicity.copy(itemText = "Vomiting", itemAnswerValueValueString = ""),
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
                code5ATCCode = FULL_ATC_CODE,
                code5ATCDisplay = "PARACETAMOL",
                administrationRoute = "oraal"
            ), medicationEntry(
                status = "",
                dosageInstruction = "Irrelevant",
                start = LocalDate.of(2019, 2, 2),
                end = LocalDate.of(2019, 4, 4),
                active = false,
                code5ATCDisplay = ""
            )
        )
    }

    fun medicationEntry(
        status: String, dosageInstruction: String, start: LocalDate, end: LocalDate, active: Boolean,
        code5ATCDisplay: String = "",
        administrationRoute: String = "",
        code5ATCCode: String = "",
        codeText: String = ""
    ): MedicationEntry {
        return MedicationEntry(
            status = status,
            dosageInstructionText = dosageInstruction,
            periodOfUseValuePeriodStart = start,
            periodOfUseValuePeriodEnd = end,
            active = active,
            code5ATCDisplay = code5ATCDisplay,
            subject = TEST_SUBJECT,
            codeText = codeText,
            code5ATCCode = code5ATCCode,
            chemicalSubgroupDisplay = "",
            pharmacologicalSubgroupDisplay = "",
            therapeuticSubgroupDisplay = "",
            anatomicalMainGroupDisplay = "",
            dosageInstructionRouteDisplay = administrationRoute,
            dosageInstructionDoseQuantityUnit = "",
            dosageInstructionDoseQuantityValue = 0.0,
            dosageInstructionFrequencyUnit = "",
            dosageInstructionFrequencyValue = 0.0,
            dosageInstructionMaxDosePerAdministration = 0.0,
            dosageInstructionPatientInstruction = "",
            dosageInstructionAsNeededDisplay = "",
            dosageInstructionPeriodBetweenDosagesUnit = "",
            dosageInstructionPeriodBetweenDosagesValue = 0.0,
            dosageDoseValue = "",
            dosageRateQuantityUnit = "",
            dosageDoseUnitDisplayOriginal = "",
            stopTypeDisplay = ""
        )
    }

    fun createTestLabEntries(): List<LabEntry> {
        return listOf(
            LabEntry(
                subject = TEST_SUBJECT,
                valueQuantityComparator = "",
                codeCodeOriginal = "LAB1",
                codeDisplayOriginal = "Lab Value 1",
                valueQuantityValue = 30.0,
                valueQuantityUnit = "U/l",
                referenceRangeText = "20 - 40",
                effectiveDateTime = LocalDate.of(2018, 5, 29),
            ), LabEntry(
                subject = TEST_SUBJECT,
                valueQuantityComparator = "",
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
                valueQuantityComparator = "",
                codeCodeOriginal = "LAB4",
                codeDisplayOriginal = "Lab Value 4",
                valueQuantityValue = 20.0,
                valueQuantityUnit = "mL/min",
                referenceRangeText = "",
                effectiveDateTime = LocalDate.of(2018, 5, 29),
            )
        )
    }

    private fun createTestVitalFunctionEntries(): List<VitalFunctionEntry> {
        return listOf(
            VitalFunctionEntry(
                subject = TEST_SUBJECT,
                effectiveDateTime = LocalDateTime.of(2021, 2, 27, 12, 30, 0, 0),
                codeDisplayOriginal = "NIBP",
                componentCodeDisplay = "Systolic blood pressure",
                quantityUnit = "mmHg",
                quantityValue = 120.0

            ),
            VitalFunctionEntry(
                subject = TEST_SUBJECT,
                effectiveDateTime = LocalDateTime.of(2021, 2, 26, 12, 30, 0, 0),
                codeDisplayOriginal = "NIBP",
                componentCodeDisplay = "Systolic blood pressure",
                quantityUnit = "mmHg",
                quantityValue = 1200.0
            ),
            VitalFunctionEntry(
                subject = TEST_SUBJECT,
                effectiveDateTime = LocalDateTime.of(2021, 2, 27, 12, 30, 0, 0),
                codeDisplayOriginal = "NIBP",
                componentCodeDisplay = "Systolic blood pressure",
                quantityUnit = "mmHg",
                quantityValue = 120.0
            ),
            VitalFunctionEntry(
                subject = TEST_SUBJECT,
                effectiveDateTime = LocalDateTime.of(2021, 2, 27, 12, 30, 0, 0),
                codeDisplayOriginal = "NIBP",
                componentCodeDisplay = "Diastolic blood pressure",
                quantityUnit = "mmHg",
                quantityValue = 120.0,
            )
        )
    }

    private fun createTestIntoleranceEntries(): List<IntoleranceEntry> {
        return listOf(
            IntoleranceEntry(
                subject = TEST_SUBJECT,
                assertedDate = LocalDate.of(2018, 1, 1),
                category = "medication",
                categoryAllergyCategoryDisplay = "",
                clinicalStatus = "",
                verificationStatus = "",
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
                effectiveDateTime = LocalDateTime.of(2018, 4, 5, 12, 30, 0)
            ), BodyWeightEntry(
                subject = TEST_SUBJECT,
                valueQuantityValue = 61.1,
                valueQuantityUnit = "kilogram",
                effectiveDateTime = LocalDateTime.of(2018, 5, 5, 12, 30, 0)
            ), BodyWeightEntry(
                subject = TEST_SUBJECT,
                valueQuantityValue = 61.1,
                valueQuantityUnit = "kilogram",
                effectiveDateTime = LocalDateTime.of(2018, 5, 5, 12, 30, 0)
            ), BodyWeightEntry(
                subject = TEST_SUBJECT,
                valueQuantityValue = 611.0,
                valueQuantityUnit = "kilogram",
                effectiveDateTime = LocalDateTime.of(2018, 5, 4, 12, 30, 0)
            )
        )
    }
}