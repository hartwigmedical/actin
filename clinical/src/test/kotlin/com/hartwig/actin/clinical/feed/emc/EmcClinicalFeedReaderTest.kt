package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.ClinicalFeedReader.read
import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.datamodel.clinical.Gender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

class EmcClinicalFeedReaderTest {

    @Test
    @Throws(IOException::class)
    fun canReadFromTestDirectory() {
        val feed = read(FEED_DIRECTORY)
        assertPatients(feed.patientEntries)
        assertQuestionnaires(feed.questionnaireEntries)
        assertSurgeries(feed.surgeryEntries)
        assertMedication(feed.medicationEntries)
        assertLab(feed.labEntries)
        assertVitalFunctions(feed.vitalFunctionEntries)
        assertIntolerances(feed.intoleranceEntries)
        assertBodyWeights(feed.bodyWeightEntries)
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private const val PATIENT = "ACTN01029999"

        private fun assertPatients(entries: List<PatientEntry>) {
            assertThat(entries.size).isEqualTo(1)
            val entry = entries[0]
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.birthYear).isEqualTo(1953)
            assertThat(entry.gender).isEqualTo(Gender.MALE)
            assertThat(entry.periodStart).isEqualTo(LocalDate.of(2020, 7, 13))
            assertThat(entry.periodEnd).isNull()
        }

        private fun assertQuestionnaires(entries: List<QuestionnaireEntry>) {
            assertThat(entries.size).isEqualTo(1)
            val entry = findByAuthoredDate(entries, LocalDate.of(2021, 8, 16))

            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.description).isEqualTo("INT Consult")
            assertThat(entry.itemText).isEqualTo("Beloop")
            assertThat(entry.text.split("\\n").dropLastWhile { it.isEmpty() }.toTypedArray().size.toLong()).isEqualTo(37)

            assertThat(entry.text).startsWith("ACTIN Questionnaire")
            assertThat(entry.text).contains("CNS lesions")
            assertThat(entry.text).contains("Cancer-related complications (e.g. pleural effusion)")
        }

        private fun findByAuthoredDate(entries: List<QuestionnaireEntry>, dateToFind: LocalDate): QuestionnaireEntry {
            for (entry in entries) {
                if (entry.authored == dateToFind) {
                    return entry
                }
            }
            throw IllegalStateException("No questionnaire entry found for date '$dateToFind'")
        }

        private fun assertSurgeries(entries: List<SurgeryEntry>) {
            assertThat(entries.size).isEqualTo(2)
            val entry = entries[0]
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.classDisplay).isEqualTo("surgery")
            assertThat(entry.periodStart).isEqualTo(LocalDate.of(2020, 8, 28))
            assertThat(entry.periodEnd).isEqualTo(LocalDate.of(2020, 8, 28))
            assertThat(entry.codeCodingDisplayOriginal).isEqualTo("diagnostics stomach")
            assertThat(entry.encounterStatus).isEqualTo("planned")
            assertThat(entry.procedureStatus).isEqualTo("planned")
            assertThat(entries[1].codeCodingDisplayOriginal).isEqualTo("Geen ingreep- operatie uitgesteld")
        }

        private fun assertMedication(entries: List<MedicationEntry>) {
            assertThat(entries.size).isEqualTo(1)
            val entry = entries[0]
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.codeText).isEqualTo("19-0716 PEMBROLIZUMAB V/P INFOPL 25MG/ML FL 4ML")
            assertThat(entry.code5ATCDisplay).isEmpty()
            assertThat(entry.dosageInstructionDoseQuantityUnit).isEqualTo("MILLIGRAM")
            assertThat(entry.dosageInstructionDoseQuantityValue).isEqualTo(200.0, withPrecision(EPSILON))
            assertThat(entry.dosageInstructionFrequencyUnit).isEmpty()
            assertDoubleEquals(0.0, entry.dosageInstructionFrequencyValue)
            assertDoubleEquals(0.0, entry.dosageInstructionMaxDosePerAdministration)
            assertThat(entry.dosageInstructionPatientInstruction).isEqualTo("Excreta: nvt")
            assertThat(entry.dosageInstructionAsNeededDisplay).isEmpty()
            assertThat(entry.dosageInstructionPeriodBetweenDosagesUnit).isEmpty()
            assertDoubleEquals(0.0, entry.dosageInstructionPeriodBetweenDosagesValue)
            assertThat(entry.dosageInstructionText).isEqualTo("200 milligram inlooptijd: 30 minuten, via 0,2 um filter")
            assertThat(entry.status).isEmpty()
            assertThat(entry.active).isNull()
            assertThat(entry.dosageDoseValue).isEmpty()
            assertThat(entry.dosageRateQuantityUnit).isEmpty()
            assertThat(entry.dosageDoseUnitDisplayOriginal).isEmpty()
            assertThat(entry.periodOfUseValuePeriodStart).isEqualTo(LocalDate.of(2019, 6, 7))
            assertThat(entry.periodOfUseValuePeriodEnd).isEqualTo(LocalDate.of(2019, 6, 7))
            assertThat(entry.stopTypeDisplay).isEqualTo("Definitief")
        }

        private fun assertDoubleEquals(expected: Double, actual: Double?) {
            assertThat(actual).isNotNull()
            assertThat(actual!!).isEqualTo(expected, withPrecision(EPSILON))
        }

        private fun assertLab(entries: List<LabEntry>) {
            assertThat(entries.size).isEqualTo(1)
            val entry = findByCodeCodeOriginal(entries, "AC")
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.codeDisplayOriginal).isEqualTo("ACTH")
            assertThat(entry.valueQuantityComparator).isEmpty()
            assertThat(entry.valueQuantityValue).isEqualTo(5.5, withPrecision(EPSILON))
            assertThat(entry.valueQuantityUnit).isEqualTo("10^9/L")
            assertThat(entry.referenceRangeText).isEqualTo("3.5 - 10")
            assertThat(entry.effectiveDateTime).isEqualTo(LocalDate.of(2019, 6, 27))
        }

        private fun findByCodeCodeOriginal(entries: List<LabEntry>, codeCodeOriginal: String): LabEntry {
            return entries.find { it.codeCodeOriginal == codeCodeOriginal }
                ?: throw IllegalStateException("No lab entry found with codeCodeOriginal '$codeCodeOriginal'")
        }

        private fun assertVitalFunctions(entries: List<VitalFunctionEntry>) {
            assertThat(entries.size).isEqualTo(1)
            val entry = entries[0]
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.effectiveDateTime).isEqualTo(LocalDateTime.of(2019, 4, 28, 13, 45))
            assertThat(entry.codeDisplayOriginal).isEqualTo("NIBP")
            assertThat(entry.componentCodeDisplay).isEqualTo("Systolic blood pressure")
            assertThat(entry.quantityUnit).isEqualTo("mmHg")
            assertThat(entry.quantityValue!!).isEqualTo(108.0, withPrecision(EPSILON))
        }

        private fun assertIntolerances(entries: List<IntoleranceEntry>) {
            assertThat(entries.size).isEqualTo(1)
            val entry = entries[0]
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.assertedDate).isEqualTo(LocalDate.of(2014, 4, 21))
            assertThat(entry.category).isEqualTo("medication")
            assertThat(entry.categoryAllergyCategoryDisplay).isEqualTo("Propensity to adverse reactions to drug")
            assertThat(entry.clinicalStatus).isEqualTo("active")
            assertThat(entry.verificationStatus).isEqualTo("confirmed")
            assertThat(entry.codeText).isEqualTo("SIMVASTATINE")
            assertThat(entry.criticality).isEqualTo("low")
            assertThat(entry.isSideEffect).isEqualTo("allergy")
        }

        private fun assertBodyWeights(entries: List<BodyWeightEntry>) {
            assertThat(entries.size).isEqualTo(2)

            val entry = findByDate(entries, LocalDateTime.of(2020, 8, 11, 0, 0, 0, 0))
            assertThat(entry.subject).isEqualTo(PATIENT)
            assertThat(entry.valueQuantityValue).isEqualTo(61.1, withPrecision(EPSILON))
            assertThat(entry.valueQuantityUnit).isEqualTo("kilogram")

            val entry2 = findByDate(entries, LocalDateTime.of(2020, 8, 20, 8, 43, 0, 0))
            assertThat(entry2.subject).isEqualTo(PATIENT)
            assertThat(entry2.valueQuantityValue).isEqualTo(58.9, withPrecision(EPSILON))
            assertThat(entry2.valueQuantityUnit).isEqualTo("kilogram")
        }

        private fun findByDate(entries: List<BodyWeightEntry>, dateToFind: LocalDateTime): BodyWeightEntry {
            return entries.find { it.effectiveDateTime == dateToFind }
                ?: throw IllegalStateException("Could not find body weight entry with date '$dateToFind'")
        }
    }
}