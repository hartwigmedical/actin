package com.hartwig.actin.clinical.feed

import com.google.common.io.Resources
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.ClinicalFeedReader.read
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class ClinicalFeedReaderTest {
    @Test
    @Throws(IOException::class)
    fun canReadFromTestDirectory() {
        val feed = read(CLINICAL_FEED_DIRECTORY)
        assertPatients(feed.patientEntries())
        assertQuestionnaires(feed.questionnaireEntries())
        assertSurgeries(feed.surgeryEntries())
        assertMedication(feed.medicationEntries())
        assertLab(feed.labEntries())
        assertVitalFunctions(feed.vitalFunctionEntries())
        assertIntolerances(feed.intoleranceEntries())
        assertBodyWeights(feed.bodyWeightEntries())
    }

    companion object {
        private val CLINICAL_FEED_DIRECTORY = Resources.getResource("feed").path
        private const val EPSILON = 1.0E-10
        private fun assertPatients(entries: List<PatientEntry?>) {
            Assert.assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            Assert.assertEquals("ACTN-01-02-9999", entry!!.subject())
            Assert.assertEquals(1953, entry.birthYear().toLong())
            Assert.assertEquals(Gender.MALE, entry.gender())
            Assert.assertEquals(LocalDate.of(2020, 7, 13), entry.periodStart())
            Assert.assertNull(entry.periodEnd())
        }

        private fun assertQuestionnaires(entries: List<QuestionnaireEntry?>) {
            Assert.assertEquals(1, entries.size.toLong())
            val entry = findByAuthoredDate(entries, LocalDate.of(2021, 8, 16))
            Assert.assertEquals("ACTN-01-02-9999", entry.subject())
            Assert.assertEquals("INT Consult", entry.description())
            Assert.assertEquals("Beloop", entry.itemText())
            Assert.assertEquals(26, entry.text().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size.toLong())
            Assert.assertTrue(entry.text().startsWith("ACTIN Questionnaire"))
            Assert.assertTrue(entry.text().contains("CNS lesions yes/no/unknown"))
            Assert.assertTrue(entry.text().contains("Other (e.g. Osteoporosis, Pleural effusion)"))
        }

        private fun findByAuthoredDate(entries: List<QuestionnaireEntry>, dateToFind: LocalDate): QuestionnaireEntry {
            for (entry in entries) {
                if (entry.authored() == dateToFind) {
                    return entry
                }
            }
            throw IllegalStateException("No questionnaire entry found for date '$dateToFind'")
        }

        private fun assertSurgeries(entries: List<SurgeryEntry?>) {
            Assert.assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            Assert.assertEquals("ACTN-01-02-9999", entry!!.subject())
            Assert.assertEquals("surgery", entry.classDisplay())
            Assert.assertEquals(LocalDate.of(2020, 8, 28), entry.periodStart())
            Assert.assertEquals(LocalDate.of(2020, 8, 28), entry.periodEnd())
            Assert.assertEquals("diagnostics stomach", entry.codeCodingDisplayOriginal())
            Assert.assertEquals("planned", entry.encounterStatus())
            Assert.assertEquals("planned", entry.procedureStatus())
        }

        private fun assertMedication(entries: List<MedicationEntry?>) {
            Assert.assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            Assert.assertEquals("ACTN-01-02-9999", entry!!.subject())
            Assert.assertEquals("19-0716 PEMBROLIZUMAB V/P INFOPL 25MG/ML FL 4ML", entry.codeText())
            Assert.assertTrue(entry.code5ATCDisplay().isEmpty())
            Assert.assertEquals("MILLIGRAM", entry.dosageInstructionDoseQuantityUnit())
            Assert.assertEquals(200.0, entry.dosageInstructionDoseQuantityValue(), EPSILON)
            Assert.assertTrue(entry.dosageInstructionFrequencyUnit().isEmpty())
            assertDoubleEquals(0.0, entry.dosageInstructionFrequencyValue())
            assertDoubleEquals(0.0, entry.dosageInstructionMaxDosePerAdministration())
            Assert.assertEquals("Excreta: nvt", entry.dosageInstructionPatientInstruction())
            Assert.assertTrue(entry.dosageInstructionAsNeededDisplay().isEmpty())
            Assert.assertTrue(entry.dosageInstructionPeriodBetweenDosagesUnit().isEmpty())
            assertDoubleEquals(0.0, entry.dosageInstructionPeriodBetweenDosagesValue())
            Assert.assertEquals("200 milligram inlooptijd: 30 minuten, via 0,2 um filter", entry.dosageInstructionText())
            Assert.assertTrue(entry.status().isEmpty())
            Assert.assertNull(entry.active())
            Assert.assertTrue(entry.dosageDoseValue().isEmpty())
            Assert.assertTrue(entry.dosageRateQuantityUnit().isEmpty())
            Assert.assertTrue(entry.dosageDoseUnitDisplayOriginal().isEmpty())
            Assert.assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodStart())
            Assert.assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodEnd())
            Assert.assertEquals("Definitief", entry.stopTypeDisplay())
        }

        private fun assertDoubleEquals(expected: Double, actual: Double?) {
            Assert.assertNotNull(actual)
            Assert.assertEquals(expected, actual!!, EPSILON)
        }

        private fun assertLab(entries: List<LabEntry?>) {
            Assert.assertEquals(2, entries.size.toLong())
            val entry1 = findByCodeCodeOriginal(entries, "LEUKO-ABS")
            Assert.assertEquals("ACTN-01-02-9999", entry1.subject())
            Assert.assertEquals("Leukocytes", entry1.codeDisplayOriginal())
            Assert.assertEquals(Strings.EMPTY, entry1.valueQuantityComparator())
            Assert.assertEquals(5.5, entry1.valueQuantityValue(), EPSILON)
            Assert.assertEquals("10^9/L", entry1.valueQuantityUnit())
            Assert.assertEquals("3.5 - 10", entry1.referenceRangeText())
            Assert.assertEquals(LocalDate.of(2019, 6, 27), entry1.effectiveDateTime())
            val entry2 = findByCodeCodeOriginal(entries, "HB")
            Assert.assertEquals("ACTN-01-02-9999", entry2.subject())
            Assert.assertEquals("Hemoglobine", entry2.codeDisplayOriginal())
            Assert.assertEquals(Strings.EMPTY, entry2.valueQuantityComparator())
            Assert.assertEquals(4.2, entry2.valueQuantityValue(), EPSILON)
            Assert.assertEquals("mmol/L", entry2.valueQuantityUnit())
            Assert.assertEquals("8.8 - 10.7", entry2.referenceRangeText())
            Assert.assertEquals(LocalDate.of(2019, 5, 27), entry2.effectiveDateTime())
        }

        private fun findByCodeCodeOriginal(entries: List<LabEntry>, codeCodeOriginal: String): LabEntry {
            for (entry in entries) {
                if (entry.codeCodeOriginal() == codeCodeOriginal) {
                    return entry
                }
            }
            throw IllegalStateException("No lab entry found with codeCodeOriginal '$codeCodeOriginal'")
        }

        private fun assertVitalFunctions(entries: List<VitalFunctionEntry?>) {
            Assert.assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            Assert.assertEquals("ACTN-01-02-9999", entry!!.subject())
            Assert.assertEquals(LocalDate.of(2019, 4, 28), entry.effectiveDateTime())
            Assert.assertEquals("NIBP", entry.codeDisplayOriginal())
            Assert.assertEquals("Systolic blood pressure", entry.componentCodeDisplay())
            Assert.assertEquals("mm[Hg]", entry.quantityUnit())
            Assert.assertEquals(108.0, entry.quantityValue(), EPSILON)
        }

        private fun assertIntolerances(entries: List<IntoleranceEntry?>) {
            Assert.assertEquals(1, entries.size.toLong())
            val entry = entries[0]
            Assert.assertEquals("ACTN-01-02-9999", entry!!.subject())
            Assert.assertEquals(LocalDate.of(2014, 4, 21), entry.assertedDate())
            Assert.assertEquals("medication", entry.category())
            Assert.assertEquals("Propensity to adverse reactions to drug", entry.categoryAllergyCategoryDisplay())
            Assert.assertEquals("active", entry.clinicalStatus())
            Assert.assertEquals("confirmed", entry.verificationStatus())
            Assert.assertEquals("SIMVASTATINE", entry.codeText())
            Assert.assertEquals("low", entry.criticality())
            assertEquals("allergy", entry.isSideEffect)
        }

        private fun assertBodyWeights(entries: List<BodyWeightEntry?>) {
            Assert.assertEquals(2, entries.size.toLong())
            val entry1 = findByDate(entries, LocalDate.of(2020, 8, 11))
            Assert.assertEquals("ACTN-01-02-9999", entry1.subject())
            Assert.assertEquals(61.1, entry1.valueQuantityValue(), EPSILON)
            Assert.assertEquals("kilogram", entry1.valueQuantityUnit())
            val entry2 = findByDate(entries, LocalDate.of(2020, 8, 20))
            Assert.assertEquals("ACTN-01-02-9999", entry2.subject())
            Assert.assertEquals(58.9, entry2.valueQuantityValue(), EPSILON)
            Assert.assertEquals("kilogram", entry2.valueQuantityUnit())
        }

        private fun findByDate(entries: List<BodyWeightEntry>, dateToFind: LocalDate): BodyWeightEntry {
            for (entry in entries) {
                if (entry.effectiveDateTime() == dateToFind) {
                    return entry
                }
            }
            throw IllegalStateException("Could not find body weight entry with date '$dateToFind'")
        }
    }
}