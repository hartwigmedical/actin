package com.hartwig.actin.clinical

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.ClinicalRecordsFactory.Companion.toPatientId
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicityEvaluation
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.feed.TestFeedFactory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class ClinicalRecordsFactoryTest {
    @Test
    fun canGeneratePatientIds() {
        assertEquals("ACTN01029999", toPatientId("ACTN-01-02-9999"))
        assertEquals("ACTN01029999", toPatientId("01-02-9999"))
    }

    @Test
    fun canCreateClinicalRecordsFromMinimalTestData() {
        val records = createMinimalTestClinicalRecords()
        assertEquals(1, records.size.toLong())
        assertEquals(TEST_PATIENT, records[0].patientId())
    }

    @Test
    fun canCreateClinicalRecordsFromProperTestData() {
        val records = createProperTestClinicalRecords()
        assertEquals(1, records.size.toLong())
        val record = records[0]
        assertEquals(TEST_PATIENT, record.patientId())
        assertPatientDetails(record.patient())
        assertTumorDetails(record.tumor())
        assertClinicalStatus(record.clinicalStatus())
        assertToxicities(record.toxicities())
        assertToxicityEvaluations(record.toxicityEvaluations())
        assertAllergies(record.intolerances())
        assertSurgeries(record.surgeries())
        assertSurgicalTreatments(record.surgicalTreatments())
        assertBodyWeights(record.bodyWeights())
        assertVitalFunctions(record.vitalFunctions())
        assertBloodTransfusions(record.bloodTransfusions())
        assertMedications(record.medications())
    }

    companion object {
        private const val TEST_PATIENT = "ACTN01029999"
        private const val EPSILON = 1.0E-10
        private fun assertPatientDetails(patient: PatientDetails) {
            assertEquals(1960, patient.birthYear().toLong())
            assertEquals(Gender.MALE, patient.gender())
            assertEquals(LocalDate.of(2021, 6, 1), patient.registrationDate())
            assertEquals(LocalDate.of(2021, 8, 1), patient.questionnaireDate())
            assertEquals("GAYA-01-02-9999", patient.otherMolecularPatientId())
        }

        private fun assertTumorDetails(tumor: TumorDetails) {
            assertNull(tumor.primaryTumorLocation())
            assertNull(tumor.primaryTumorSubLocation())
            assertNull(tumor.primaryTumorType())
            assertNull(tumor.primaryTumorSubType())
            assertNull(tumor.primaryTumorExtraDetails())
            assertNull(tumor.doids())
            assertEquals(TumorStage.IV, tumor.stage())
            assertTrue(tumor.hasMeasurableDisease()!!)
            assertTrue(tumor.hasBrainLesions()!!)
            assertTrue(tumor.hasActiveBrainLesions()!!)
            assertNull(tumor.hasCnsLesions())
            assertNull(tumor.hasActiveCnsLesions())
            assertFalse(tumor.hasBoneLesions()!!)
            assertFalse(tumor.hasLiverLesions()!!)
            assertTrue(tumor.hasLungLesions()!!)
            assertTrue(tumor.otherLesions()!!.contains("Abdominal"))
            assertEquals("Lymph node", tumor.biopsyLocation())
        }

        private fun assertClinicalStatus(clinicalStatus: ClinicalStatus) {
            assertEquals(0, (clinicalStatus.who() as Int).toLong())
            val infectionStatus = clinicalStatus.infectionStatus()
            assertNotNull(infectionStatus)
            assertFalse(infectionStatus!!.hasActiveInfection())
            val ecg = clinicalStatus.ecg()
            assertNotNull(ecg)
            assertTrue(ecg!!.hasSigAberrationLatestECG())
            assertEquals("Sinus", ecg.aberrationDescription())
            assertEquals(clinicalStatus.hasComplications(), true)
        }

        private fun assertToxicities(toxicities: List<Toxicity>) {
            assertEquals(2, toxicities.size.toLong())
            val toxicity1 = findByName(toxicities, "Nausea")
            assertEquals(ToxicitySource.EHR, toxicity1.source())
            assertEquals(2, (toxicity1.grade() as Int).toLong())
            val toxicity2 = findByName(toxicities, "Pain")
            assertEquals(ToxicitySource.EHR, toxicity2.source())
            assertEquals(0, (toxicity2.grade() as Int).toLong())
        }

        private fun findByName(toxicities: List<Toxicity>, name: String): Toxicity {
            return toxicities.find { it.name() == name }!!
        }

        private fun assertToxicityEvaluations(toxicityEvaluations: List<ToxicityEvaluation>?) {
            assertNotNull(toxicityEvaluations)
            assertEquals(2, toxicityEvaluations!!.size.toLong())
            val toxicity1 = findToxicityEvaluationByName(toxicityEvaluations, "Nausea")
            assertEquals(ToxicitySource.EHR, toxicity1.source())
            assertEquals(2, (toxicity1.toxicities().iterator().next().grade() as Int).toLong())
            val toxicity2 = findToxicityEvaluationByName(toxicityEvaluations, "Pain")
            assertEquals(ToxicitySource.EHR, toxicity2.source())
            assertEquals(0, (toxicity2.toxicities().iterator().next().grade() as Int).toLong())
        }

        private fun findToxicityEvaluationByName(toxicityEvaluations: List<ToxicityEvaluation>, name: String): ToxicityEvaluation {
            return toxicityEvaluations.find { it.toxicities().iterator().next().name() == name }!!
        }

        private fun assertAllergies(allergies: List<Intolerance>) {
            assertEquals(1, allergies.size.toLong())
            val intolerance = allergies[0]
            assertEquals("Pills", intolerance.name())
            assertEquals("Medication", intolerance.category())
            assertEquals("Unknown", intolerance.criticality())
        }

        private fun assertSurgeries(surgeries: List<Surgery>) {
            assertEquals(1, surgeries.size.toLong())
            val surgery = surgeries[0]
            assertEquals(LocalDate.of(2015, 10, 10), surgery.endDate())
            assertEquals(SurgeryStatus.PLANNED, surgery.status())
        }

        private fun assertSurgicalTreatments(surgicalTreatments: List<TreatmentHistoryEntry>?) {
            assertNotNull(surgicalTreatments)
            assertEquals(1, surgicalTreatments!!.size.toLong())
            val surgery = surgicalTreatments[0].surgeryHistoryDetails()
            assertNotNull(surgery)
            assertEquals(LocalDate.of(2015, 10, 10), surgery!!.endDate())
            assertEquals(SurgeryStatus.PLANNED, surgery.status())
        }

        private fun assertBodyWeights(bodyWeights: List<BodyWeight>) {
            assertEquals(2, bodyWeights.size.toLong())
            val bodyWeight1 = findByDate(bodyWeights, LocalDate.of(2018, 4, 5))
            assertEquals(58.1, bodyWeight1.value(), EPSILON)
            assertEquals("kilogram", bodyWeight1.unit())
            val bodyWeight2 = findByDate(bodyWeights, LocalDate.of(2018, 5, 5))
            assertEquals(61.1, bodyWeight2.value(), EPSILON)
            assertEquals("kilogram", bodyWeight2.unit())
        }

        private fun findByDate(bodyWeights: List<BodyWeight>, dateToFind: LocalDate): BodyWeight {
            for (bodyWeight in bodyWeights) {
                if (bodyWeight.date() == dateToFind) {
                    return bodyWeight
                }
            }
            throw IllegalStateException("Could not find body weight with date '$dateToFind'")
        }

        private fun assertVitalFunctions(vitalFunctions: List<VitalFunction>) {
            assertEquals(1, vitalFunctions.size.toLong())
            val vitalFunction = vitalFunctions[0]
            assertEquals(LocalDate.of(2021, 2, 27), vitalFunction.date())
            assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, vitalFunction.category())
            assertEquals("systolic", vitalFunction.subcategory())
            assertEquals(120.0, vitalFunction.value(), EPSILON)
            assertEquals("mm[Hg]", vitalFunction.unit())
        }

        private fun assertBloodTransfusions(bloodTransfusions: List<BloodTransfusion>) {
            assertEquals(1, bloodTransfusions.size.toLong())
            val bloodTransfusion = bloodTransfusions[0]
            assertEquals(LocalDate.of(2020, 7, 7), bloodTransfusion.date())
            assertEquals("Translated product", bloodTransfusion.product())
        }

        private fun assertMedications(medications: List<Medication>) {
            assertEquals(1, medications.size.toLong())
            val medication = medications[0]
            assertEquals("Paracetamol", medication.name())
            assertEquals(Sets.newHashSet("Acetanilide derivatives"), medication.categories())
            assertEquals(MedicationStatus.ACTIVE, medication.status())
            assertEquals(50.0, medication.dosage().dosageMin()!!, EPSILON)
            assertEquals(60.0, medication.dosage().dosageMax()!!, EPSILON)
            assertEquals("mg", medication.dosage().dosageUnit())
            assertEquals("day", medication.dosage().frequencyUnit())
            assertEquals(1.0, medication.dosage().periodBetweenValue())
            assertEquals("months", medication.dosage().periodBetweenUnit())
            assertFalse(medication.dosage().ifNeeded()!!)
            assertEquals(LocalDate.of(2019, 2, 2), medication.startDate())
            assertEquals(LocalDate.of(2019, 4, 4), medication.stopDate())
        }

        private fun createMinimalTestClinicalRecords(): List<ClinicalRecord> {
            return ClinicalRecordsFactory(
                TestFeedFactory.createMinimalTestFeedModel(),
                TestCurationFactory.createMinimalTestCurationModel()
            ).create()
        }

        private fun createProperTestClinicalRecords(): List<ClinicalRecord> {
            return ClinicalRecordsFactory(
                TestFeedFactory.createProperTestFeedModel(),
                TestCurationFactory.createProperTestCurationModel()
            ).create()
        }
    }
}