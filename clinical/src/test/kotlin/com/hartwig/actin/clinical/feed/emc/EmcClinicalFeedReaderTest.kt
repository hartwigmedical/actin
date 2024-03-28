package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.emc.ClinicalFeedReader.read
import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.testutil.ResourceLocator
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class EmcClinicalFeedReaderTest {
    @Test
    @Throws(IOException::class)
    fun canReadFromTestDirectory() {
        val feed = read(CLINICAL_FEED_DIRECTORY)
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
        private val CLINICAL_FEED_DIRECTORY = ResourceLocator().onClasspath("feed/emc")
        private const val EPSILON = 1.0E-10
        private const val PATIENT = "ACTN01029999"

        private fun assertPatients(entries: List<PatientEntry>) {
            assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            assertEquals(PATIENT, entry.subject)
            assertEquals(1953, entry.birthYear.toLong())
            assertEquals(Gender.MALE, entry.gender)
            assertEquals(LocalDate.of(2020, 7, 13), entry.periodStart)
            assertNull(entry.periodEnd)
        }

        private fun assertQuestionnaires(entries: List<QuestionnaireEntry>) {
            assertEquals(1, entries.size.toLong())
            val entry = findByAuthoredDate(entries, LocalDate.of(2021, 8, 16))
            assertEquals(PATIENT, entry.subject)
            assertEquals("INT Consult", entry.description)
            assertEquals("Beloop", entry.itemText)
            assertEquals(26, entry.text.split("\\n").dropLastWhile { it.isEmpty() }.toTypedArray().size.toLong())
            assertTrue(entry.text.startsWith("ACTIN Questionnaire"))
            assertTrue(entry.text.contains("CNS lesions yes/no/unknown"))
            assertTrue(entry.text.contains("Other (e.g. Osteoporosis, Pleural effusion)"))
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
            assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            assertEquals(PATIENT, entry.subject)
            assertEquals("surgery", entry.classDisplay)
            assertEquals(LocalDate.of(2020, 8, 28), entry.periodStart)
            assertEquals(LocalDate.of(2020, 8, 28), entry.periodEnd)
            assertEquals("diagnostics stomach", entry.codeCodingDisplayOriginal)
            assertEquals("planned", entry.encounterStatus)
            assertEquals("planned", entry.procedureStatus)
        }

        private fun assertMedication(entries: List<MedicationEntry>) {
            assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            assertEquals(PATIENT, entry.subject)
            assertEquals("19-0716 PEMBROLIZUMAB V/P INFOPL 25MG/ML FL 4ML", entry.codeText)
            assertTrue(entry.code5ATCDisplay.isEmpty())
            assertEquals("MILLIGRAM", entry.dosageInstructionDoseQuantityUnit)
            assertEquals(200.0, entry.dosageInstructionDoseQuantityValue, EPSILON)
            assertTrue(entry.dosageInstructionFrequencyUnit.isEmpty())
            assertDoubleEquals(0.0, entry.dosageInstructionFrequencyValue)
            assertDoubleEquals(0.0, entry.dosageInstructionMaxDosePerAdministration)
            assertEquals("Excreta: nvt", entry.dosageInstructionPatientInstruction)
            assertTrue(entry.dosageInstructionAsNeededDisplay.isEmpty())
            assertTrue(entry.dosageInstructionPeriodBetweenDosagesUnit.isEmpty())
            assertDoubleEquals(0.0, entry.dosageInstructionPeriodBetweenDosagesValue)
            assertEquals("200 milligram inlooptijd: 30 minuten, via 0,2 um filter", entry.dosageInstructionText)
            assertTrue(entry.status.isEmpty())
            assertNull(entry.active)
            assertTrue(entry.dosageDoseValue.isEmpty())
            assertTrue(entry.dosageRateQuantityUnit.isEmpty())
            assertTrue(entry.dosageDoseUnitDisplayOriginal.isEmpty())
            assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodStart)
            assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodEnd)
            assertEquals("Definitief", entry.stopTypeDisplay)
        }

        private fun assertDoubleEquals(expected: Double, actual: Double?) {
            assertNotNull(actual)
            assertEquals(expected, actual!!, EPSILON)
        }

        private fun assertLab(entries: List<LabEntry>) {
            assertEquals(1, entries.size.toLong())
            val entry1 = findByCodeCodeOriginal(entries, "AC")
            assertEquals(PATIENT, entry1.subject)
            assertEquals("ACTH", entry1.codeDisplayOriginal)
            assertEquals(Strings.EMPTY, entry1.valueQuantityComparator)
            assertEquals(5.5, entry1.valueQuantityValue, EPSILON)
            assertEquals("10^9/L", entry1.valueQuantityUnit)
            assertEquals("3.5 - 10", entry1.referenceRangeText)
            assertEquals(LocalDate.of(2019, 6, 27), entry1.effectiveDateTime)
        }

        private fun findByCodeCodeOriginal(entries: List<LabEntry>, codeCodeOriginal: String): LabEntry {
            return entries.find { it.codeCodeOriginal == codeCodeOriginal }
                ?: throw IllegalStateException("No lab entry found with codeCodeOriginal '$codeCodeOriginal'")
        }

        private fun assertVitalFunctions(entries: List<VitalFunctionEntry>) {
            assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            assertEquals(PATIENT, entry.subject)
            assertEquals(LocalDateTime.of(2019, 4, 28, 13, 45), entry.effectiveDateTime)
            assertEquals("NIBP", entry.codeDisplayOriginal)
            assertEquals("Systolic blood pressure", entry.componentCodeDisplay)
            assertEquals("mmHg", entry.quantityUnit)
            assertEquals(108.0, entry.quantityValue!!, EPSILON)
        }

        private fun assertIntolerances(entries: List<IntoleranceEntry>) {
            assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            assertEquals(PATIENT, entry.subject)
            assertEquals(LocalDate.of(2014, 4, 21), entry.assertedDate)
            assertEquals("medication", entry.category)
            assertEquals("Propensity to adverse reactions to drug", entry.categoryAllergyCategoryDisplay)
            assertEquals("active", entry.clinicalStatus)
            assertEquals("confirmed", entry.verificationStatus)
            assertEquals("SIMVASTATINE", entry.codeText)
            assertEquals("low", entry.criticality)
            assertEquals("allergy", entry.isSideEffect)
        }

        private fun assertBodyWeights(entries: List<BodyWeightEntry>) {
            assertEquals(2, entries.size.toLong())
            val entry1 = findByDate(entries, LocalDateTime.of(2020, 8, 11, 0, 0, 0, 0))
            assertEquals(PATIENT, entry1.subject)
            assertEquals(61.1, entry1.valueQuantityValue, EPSILON)
            assertEquals("kilogram", entry1.valueQuantityUnit)
            val entry2 = findByDate(entries, LocalDateTime.of(2020, 8, 20, 8, 43, 0, 0))
            assertEquals(PATIENT, entry2.subject)
            assertEquals(58.9, entry2.valueQuantityValue, EPSILON)
            assertEquals("kilogram", entry2.valueQuantityUnit)
        }

        private fun findByDate(entries: List<BodyWeightEntry>, dateToFind: LocalDateTime): BodyWeightEntry {
            return entries.find { it.effectiveDateTime == dateToFind }
                ?: throw IllegalStateException("Could not find body weight entry with date '$dateToFind'")
        }
    }
}