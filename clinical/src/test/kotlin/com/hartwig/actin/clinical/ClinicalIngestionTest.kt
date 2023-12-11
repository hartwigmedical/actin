package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.ClinicalIngestion.Companion.toPatientId
import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.FULL_ATC_CODE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import com.hartwig.actin.clinical.feed.TestFeedFactory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ClinicalIngestionTest {

    @Test
    fun `Should generate patient ids`() {
        assertEquals("ACTN01029999", toPatientId("ACTN-01-02-9999"))
        assertEquals("ACTN01029999", toPatientId("01-02-9999"))
    }

    @Test
    fun `Should ingest minimal test data with no warnings and WARN_NO_QUESTIONNAIRE status`() {
        val results = createMinimalTestIngestionResults()
        assertEquals(1, results.size.toLong())
        assertThat(results[0].patientId).isEqualTo(TEST_PATIENT)
        assertThat(results[0].status).isEqualTo(IngestionStatus.WARN_NO_QUESTIONNAIRE)
        assertThat(results[0].curationResults).isEmpty()
        assertThat(results[0].clinicalRecord).isNotNull()
    }

    @Test
    fun `Should ingest proper test data with warnings and WARN_CURATION_REQUIRED status`() {
        val results = createProperTestIngestionResults()
        assertThat(results).hasSize(1)
        assertThat(results[0].patientId).isEqualTo(TEST_PATIENT)
        assertThat(results[0].status).isEqualTo(IngestionStatus.WARN_CURATION_REQUIRED)
        assertThat(results[0].curationResults).containsExactlyInAnyOrder(
            CurationResult(
                "Toxicity Translation",
                listOf(
                    CurationRequirement("Nausea", "No translation found for toxicity: 'Nausea'"),
                    CurationRequirement("Pain", "No translation found for toxicity: 'Pain'")
                )
            ),
            CurationResult("Toxicity", listOf(CurationRequirement("toxic", "Could not find toxicity config for input 'toxic'"))),
            CurationResult(
                "Primary Tumor",
                listOf(CurationRequirement("ovary | serous", "Could not find primary tumor config for input 'ovary | serous'"))
            ),
            CurationResult("Infection", listOf(CurationRequirement("No", "Could not find infection config for input 'No'"))),
            CurationResult("ECG", listOf(CurationRequirement("Sinus", "Could not find ECG config for input 'Sinus'"))),
            CurationResult(
                "Oncological History",
                listOf(
                    CurationRequirement("cisplatin", "Could not find treatment history or second primary config for input 'cisplatin'"),
                    CurationRequirement("nivolumab", "Could not find treatment history or second primary config for input 'nivolumab'"),
                    CurationRequirement("surgery", "Could not find treatment history or second primary config for input 'surgery'")
                )
            ),
            CurationResult(
                "Second Primary",
                listOf(
                    CurationRequirement("surgery", "Could not find second primary or treatment history config for input 'surgery'"),
                    CurationRequirement(
                        "sarcoma | Feb 2020",
                        "Could not find second primary or treatment history config for input 'sarcoma | Feb 2020'"
                    )
                )
            ),
            CurationResult(
                "Non Oncological History",
                listOf(CurationRequirement("diabetes", "Could not find non-oncological history config for input 'diabetes'"))
            ),
            CurationResult(
                "Molecular Test",
                listOf(
                    CurationRequirement("ERBB2 3+", "Could not find IHC molecular test config for input 'ERBB2 3+'"),
                    CurationRequirement("Positive", "Could not find PD-L1 molecular test config for input 'Positive'")
                )
            ),
            CurationResult(
                "Laboratory Translation",
                listOf(
                    CurationRequirement(
                        "LAB1",
                        "Could not find laboratory translation for lab value with code 'LAB1' and name 'Lab Value 1'"
                    ),
                    CurationRequirement(
                        "LAB2",
                        "Could not find laboratory translation for lab value with code 'LAB2' and name 'Lab Value 2'"
                    ),
                    CurationRequirement(
                        "LAB3",
                        "Could not find laboratory translation for lab value with code 'LAB3' and name 'Lab Value 3'"
                    ),
                    CurationRequirement(
                        "LAB4",
                        "Could not find laboratory translation for lab value with code 'LAB4' and name 'Lab Value 4'"
                    )
                )
            ),
            CurationResult(
                "Intolerance", listOf(CurationRequirement("Pills", "Could not find intolerance config for input 'Pills'"))
            )
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
            assertEquals(2, vitalFunctions.size.toLong())
            val vitalFunction = vitalFunctions[0]
            assertEquals(LocalDate.of(2021, 2, 27), vitalFunction.date())
            assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, vitalFunction.category())
            assertEquals("systolic", vitalFunction.subcategory())
            assertEquals(120.0, vitalFunction.value(), EPSILON)
            assertEquals("mm[Hg]", vitalFunction.unit())
            val vitalFunction2 = vitalFunctions[1]
            assertEquals(1200.0, vitalFunction2.value(), EPSILON)
            assertEquals("<ignore>", vitalFunction2.unit())
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

        private fun createProperTestIngestionResults(): List<IngestionResult> {
            return ClinicalIngestion(
                TestFeedFactory.createProperTestFeedModel(),
                TestCurationFactory.createProperTestCurationDatabase(),
                TestAtcFactory.createProperAtcModel()
            ).run()
        }
    }
}