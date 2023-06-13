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
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class ClinicalRecordsFactoryTest {
    @Test
    fun canGeneratePatientIds() {
        Assert.assertEquals("ACTN01029999", toPatientId("ACTN-01-02-9999"))
        Assert.assertEquals("ACTN01029999", toPatientId("01-02-9999"))
    }

    @Test
    fun canCreateClinicalRecordsFromMinimalTestData() {
        val records = createMinimalTestClinicalRecords()
        Assert.assertEquals(1, records.size.toLong())
        Assert.assertEquals(TEST_PATIENT, records[0].patientId())
    }

    @Test
    fun canCreateClinicalRecordsFromProperTestData() {
        val records = createProperTestClinicalRecords()
        Assert.assertEquals(1, records.size.toLong())
        val record = records[0]
        Assert.assertEquals(TEST_PATIENT, record.patientId())
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
            Assert.assertEquals(1960, patient.birthYear().toLong())
            Assert.assertEquals(Gender.MALE, patient.gender())
            Assert.assertEquals(LocalDate.of(2021, 6, 1), patient.registrationDate())
            Assert.assertEquals(LocalDate.of(2021, 8, 1), patient.questionnaireDate())
            Assert.assertEquals("GAYA-01-02-9999", patient.otherMolecularPatientId())
        }

        private fun assertTumorDetails(tumor: TumorDetails) {
            Assert.assertNull(tumor.primaryTumorLocation())
            Assert.assertNull(tumor.primaryTumorSubLocation())
            Assert.assertNull(tumor.primaryTumorType())
            Assert.assertNull(tumor.primaryTumorSubType())
            Assert.assertNull(tumor.primaryTumorExtraDetails())
            Assert.assertNull(tumor.doids())
            Assert.assertEquals(TumorStage.IV, tumor.stage())
            Assert.assertTrue(tumor.hasMeasurableDisease()!!)
            Assert.assertTrue(tumor.hasBrainLesions()!!)
            Assert.assertTrue(tumor.hasActiveBrainLesions()!!)
            Assert.assertNull(tumor.hasCnsLesions())
            Assert.assertNull(tumor.hasActiveCnsLesions())
            Assert.assertFalse(tumor.hasBoneLesions()!!)
            Assert.assertFalse(tumor.hasLiverLesions()!!)
            Assert.assertTrue(tumor.hasLungLesions()!!)
            Assert.assertTrue(tumor.otherLesions()!!.contains("Abdominal"))
            Assert.assertEquals("Lymph node", tumor.biopsyLocation())
        }

        private fun assertClinicalStatus(clinicalStatus: ClinicalStatus) {
            Assert.assertEquals(0, (clinicalStatus.who() as Int).toLong())
            val infectionStatus = clinicalStatus.infectionStatus()
            Assert.assertNotNull(infectionStatus)
            Assert.assertFalse(infectionStatus!!.hasActiveInfection())
            val ecg = clinicalStatus.ecg()
            Assert.assertNotNull(ecg)
            Assert.assertTrue(ecg!!.hasSigAberrationLatestECG())
            Assert.assertEquals("Sinus", ecg.aberrationDescription())
            Assert.assertEquals(clinicalStatus.hasComplications(), true)
        }

        private fun assertToxicities(toxicities: List<Toxicity>) {
            Assert.assertEquals(2, toxicities.size.toLong())
            val toxicity1 = findByName(toxicities, "Nausea")
            Assert.assertEquals(ToxicitySource.EHR, toxicity1.source())
            Assert.assertEquals(2, (toxicity1.grade() as Int).toLong())
            val toxicity2 = findByName(toxicities, "Pain")
            Assert.assertEquals(ToxicitySource.EHR, toxicity2.source())
            Assert.assertEquals(0, (toxicity2.grade() as Int).toLong())
        }

        private fun findByName(toxicities: List<Toxicity>, name: String): Toxicity {
            return toxicities.stream()
                .filter { toxicity: Toxicity -> toxicity.name() == name }
                .findAny()
                .orElseThrow { IllegalStateException("Could not find toxicity with name: $name") }
        }

        private fun assertToxicityEvaluations(toxicityEvaluations: List<ToxicityEvaluation>?) {
            Assert.assertNotNull(toxicityEvaluations)
            Assert.assertEquals(2, toxicityEvaluations!!.size.toLong())
            val toxicity1 = findToxicityEvaluationByName(toxicityEvaluations, "Nausea")
            Assert.assertEquals(ToxicitySource.EHR, toxicity1.source())
            Assert.assertEquals(2, (toxicity1.toxicities().iterator().next().grade() as Int).toLong())
            val toxicity2 = findToxicityEvaluationByName(toxicityEvaluations, "Pain")
            Assert.assertEquals(ToxicitySource.EHR, toxicity2.source())
            Assert.assertEquals(0, (toxicity2.toxicities().iterator().next().grade() as Int).toLong())
        }

        private fun findToxicityEvaluationByName(
            toxicityEvaluations: List<ToxicityEvaluation>,
            name: String
        ): ToxicityEvaluation {
            return toxicityEvaluations.stream()
                .filter { toxicityEvaluation: ToxicityEvaluation -> toxicityEvaluation.toxicities().iterator().next().name() == name }
                .findAny()
                .orElseThrow { IllegalStateException("Could not find toxicity with name: $name") }
        }

        private fun assertAllergies(allergies: List<Intolerance>) {
            Assert.assertEquals(1, allergies.size.toLong())
            val intolerance = allergies[0]
            Assert.assertEquals("Pills", intolerance.name())
            Assert.assertEquals("Medication", intolerance.category())
            Assert.assertEquals("Unknown", intolerance.criticality())
        }

        private fun assertSurgeries(surgeries: List<Surgery>) {
            Assert.assertEquals(1, surgeries.size.toLong())
            val surgery = surgeries[0]
            Assert.assertEquals(LocalDate.of(2015, 10, 10), surgery.endDate())
            Assert.assertEquals(SurgeryStatus.PLANNED, surgery.status())
        }

        private fun assertSurgicalTreatments(surgicalTreatments: List<TreatmentHistoryEntry>?) {
            Assert.assertNotNull(surgicalTreatments)
            Assert.assertEquals(1, surgicalTreatments!!.size.toLong())
            val surgery = surgicalTreatments[0].surgeryHistoryDetails()
            Assert.assertNotNull(surgery)
            Assert.assertEquals(LocalDate.of(2015, 10, 10), surgery!!.endDate())
            Assert.assertEquals(SurgeryStatus.PLANNED, surgery.status())
        }

        private fun assertBodyWeights(bodyWeights: List<BodyWeight>) {
            Assert.assertEquals(2, bodyWeights.size.toLong())
            val bodyWeight1 = findByDate(bodyWeights, LocalDate.of(2018, 4, 5))
            Assert.assertEquals(58.1, bodyWeight1.value(), EPSILON)
            Assert.assertEquals("kilogram", bodyWeight1.unit())
            val bodyWeight2 = findByDate(bodyWeights, LocalDate.of(2018, 5, 5))
            Assert.assertEquals(61.1, bodyWeight2.value(), EPSILON)
            Assert.assertEquals("kilogram", bodyWeight2.unit())
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
            Assert.assertEquals(1, vitalFunctions.size.toLong())
            val vitalFunction = vitalFunctions[0]
            Assert.assertEquals(LocalDate.of(2021, 2, 27), vitalFunction.date())
            Assert.assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, vitalFunction.category())
            Assert.assertEquals("systolic", vitalFunction.subcategory())
            Assert.assertEquals(120.0, vitalFunction.value(), EPSILON)
            Assert.assertEquals("mm[Hg]", vitalFunction.unit())
        }

        private fun assertBloodTransfusions(bloodTransfusions: List<BloodTransfusion>) {
            Assert.assertEquals(1, bloodTransfusions.size.toLong())
            val bloodTransfusion = bloodTransfusions[0]
            Assert.assertEquals(LocalDate.of(2020, 7, 7), bloodTransfusion.date())
            Assert.assertEquals("Translated product", bloodTransfusion.product())
        }

        private fun assertMedications(medications: List<Medication>) {
            Assert.assertEquals(1, medications.size.toLong())
            val medication = medications[0]
            Assert.assertEquals("Paracetamol", medication.name())
            Assert.assertEquals(Sets.newHashSet("Acetanilide derivatives"), medication.categories())
            Assert.assertEquals(MedicationStatus.ACTIVE, medication.status())
            Assert.assertEquals(50.0, medication.dosageMin()!!, EPSILON)
            Assert.assertEquals(60.0, medication.dosageMax()!!, EPSILON)
            Assert.assertEquals("mg", medication.dosageUnit())
            Assert.assertEquals("day", medication.frequencyUnit())
            Assert.assertFalse(medication.ifNeeded()!!)
            Assert.assertEquals(LocalDate.of(2019, 2, 2), medication.startDate())
            Assert.assertEquals(LocalDate.of(2019, 4, 4), medication.stopDate())
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