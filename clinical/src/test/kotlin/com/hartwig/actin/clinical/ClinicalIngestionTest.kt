package com.hartwig.actin.clinical

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.FEED_DIRECTORY
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.ImmutableDoidManualConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val EXPECTED_CLINICAL_RECORD: String =
    "${Resources.getResource("clinical_record").path}/ACTN01029999.clinical.json"

class ClinicalIngestionTest {

    @Test
    fun `Should run ingestion from proper curation and feed files, read from filesystem`() {
        val curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(
                TestDoidModelFactory.createWithDoidManualConfig(
                    ImmutableDoidManualConfig.builder()
                        .putAdditionalDoidsPerDoid("2513", CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID)
                        .putAdditionalDoidsPerDoid("299", CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID)
                        .putAdditionalDoidsPerDoid("5082", CurationDoidValidator.DISEASE_DOID)
                        .putAdditionalDoidsPerDoid("11335", CurationDoidValidator.DISEASE_DOID)
                        .putAdditionalDoidsPerDoid("0060500", CurationDoidValidator.DISEASE_DOID).build()
                )
            ),
            TestTreatmentDatabaseFactory.createProper()
        )
        val ingestion = ClinicalIngestion.create(
            FeedModel.fromFeedDirectory(FEED_DIRECTORY),
            curationDatabase,
            TestAtcFactory.createProperAtcModel()
        )

        val record = results[0].clinicalRecord
        assertEquals(TEST_PATIENT, record.patientId())
        assertPatientDetails(record.patient())
        assertTumorDetails(record.tumor())
        assertClinicalStatus(record.clinicalStatus())
        assertToxicities(record.toxicities())
        assertAllergies(record.intolerances())
        assertSurgeries(record.surgeries())
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

        private fun assertBodyWeights(bodyWeights: List<BodyWeight>) {
            assertEquals(3, bodyWeights.size.toLong())
            val bodyWeight1 = findByDate(bodyWeights, LocalDate.of(2018, 4, 5))
            assertEquals(58.1, bodyWeight1.value(), EPSILON)
            assertEquals("kilogram", bodyWeight1.unit())
            val bodyWeight2 = findByDate(bodyWeights, LocalDate.of(2018, 5, 5))
            assertEquals(61.1, bodyWeight2.value(), EPSILON)
            assertEquals("kilogram", bodyWeight2.unit())
            val bodyWeight3 = findByDate(bodyWeights, LocalDate.of(2018, 5, 4))
            assertEquals(611.0, bodyWeight3.value(), EPSILON)
            assertEquals("<ignore>", bodyWeight3.unit())
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
            assertEquals(3, vitalFunctions.size.toLong())
            val vitalFunction = vitalFunctions[0]
            assertEquals(LocalDate.of(2021, 2, 27), vitalFunction.date())
            assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, vitalFunction.category())
            assertEquals("Systolic blood pressure", vitalFunction.subcategory())
            assertEquals(120.0, vitalFunction.value(), EPSILON)
            assertEquals("mm[Hg]", vitalFunction.unit())
            val vitalFunction2 = vitalFunctions[1]
            assertEquals(1200.0, vitalFunction2.value(), EPSILON)
            assertEquals("<ignore>", vitalFunction2.unit())
            val vitalFunction3 = vitalFunctions[2]
            assertEquals(120.0, vitalFunction3.value(), EPSILON)
            assertEquals("Diastolic blood pressure", vitalFunction3.subcategory())
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
            assertEquals(MedicationStatus.ACTIVE, medication.status())
            assertEquals(50.0, medication.dosage().dosageMin()!!, EPSILON)
            assertEquals(60.0, medication.dosage().dosageMax()!!, EPSILON)
            assertEquals("mg", medication.dosage().dosageUnit())
            assertEquals("day", medication.dosage().frequencyUnit())
            assertEquals(1.0, medication.dosage().periodBetweenValue())
            assertEquals("mo", medication.dosage().periodBetweenUnit())
            assertFalse(medication.dosage().ifNeeded()!!)
            assertEquals(LocalDate.of(2019, 2, 2), medication.startDate())
            assertEquals(LocalDate.of(2019, 4, 4), medication.stopDate())
            assertThat(medication.cypInteractions()).containsExactly(TestCurationFactory.createTestCypInteraction())
            assertThat(medication.qtProlongatingRisk()).isEqualTo(QTProlongatingRisk.POSSIBLE)
            assertThat(medication.atc()).isEqualTo(
                ImmutableAtcClassification.builder()
                    .anatomicalMainGroup(ImmutableAtcLevel.builder().code("N").name(ANATOMICAL).build())
                    .therapeuticSubGroup(ImmutableAtcLevel.builder().code("N02").name(THERAPEUTIC).build())
                    .pharmacologicalSubGroup(ImmutableAtcLevel.builder().code("N02B").name(PHARMACOLOGICAL).build())
                    .chemicalSubGroup(ImmutableAtcLevel.builder().code("N02BE").name(CHEMICAL).build())
                    .chemicalSubstance(ImmutableAtcLevel.builder().code(FULL_ATC_CODE).name(CHEMICAL_SUBSTANCE).build())
                    .build()
            )
        }

        private fun createMinimalTestIngestionResults(): List<IngestionResult> {
            return ClinicalIngestion(
                TestFeedFactory.createMinimalTestFeedModel(),
                TestCurationFactory.createMinimalTestCurationDatabase(),
                TestAtcFactory.createMinimalAtcModel()
            ).run()
        }

        val validationErrors = curationDatabase.validate()
        assertThat(validationErrors).isEmpty()

        val ingestionResult = ingestion.run()
        assertThat(ingestionResult).isNotNull
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults[0].status).isEqualTo(PatientIngestionStatus.PASS)
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo("ACTN01029999")
        assertThat(patientResults[0].curationResults).isEmpty()
        assertThat(patientResults[0].clinicalRecord).isEqualTo(ClinicalRecordJson.read(EXPECTED_CLINICAL_RECORD))
    }
}