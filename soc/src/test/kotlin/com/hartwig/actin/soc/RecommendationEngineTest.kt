package com.hartwig.actin.soc

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.*
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.soc.calendar.ReferenceDateProviderTestFactory
import com.hartwig.actin.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.soc.datamodel.Treatment
import com.hartwig.actin.soc.datamodel.TreatmentComponent
import com.hartwig.actin.soc.doid.DoidConstants
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.time.LocalDate

class RecommendationEngineTest {
    @Test
    fun shouldNotRecommendCapecitabineCombinedWithIrinotecan() {
        Assert.assertTrue(typicalTreatmentResults.none { treatment: Treatment ->
            (treatment.components.contains(TreatmentComponent.CAPECITABINE) && treatment.components.contains(TreatmentComponent.IRINOTECAN))
        })
    }

    @Test
    fun shouldNotRecommendOxaliplatinMonotherapy() {
        assertSpecificMonotherapyNotRecommended(TreatmentComponent.OXALIPLATIN)
    }

    @Test
    fun shouldNotRecommendBevacizumabMonotherapy() {
        assertSpecificMonotherapyNotRecommended(TreatmentComponent.BEVACIZUMAB)
    }

    @Test
    fun shouldNotRecommendFolfiriAterCapox() {
        Assert.assertTrue(getTreatmentResultsForPatient(patientRecordWithChemoHistory(listOf(TreatmentDB.TREATMENT_CAPOX))).none {
            it.name.equals(TreatmentDB.TREATMENT_FOLFIRI, ignoreCase = true)
        })
    }

    @Test
    fun shouldNotRecommendFolfiriAterFolfox() {
        Assert.assertTrue(getTreatmentResultsForPatient(patientRecordWithChemoHistory(listOf(TreatmentDB.TREATMENT_FOLFOX))).none {
            it.name.equals(TreatmentDB.TREATMENT_FOLFIRI, ignoreCase = true)
        })
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfterRecentTreatment() {
        CHEMO_TREATMENT_NAME_STREAM.forEach { treatmentName: String ->
            Assert.assertTrue(getTreatmentResultsForPatient(patientRecordWithChemoHistory(listOf(treatmentName))).none {
                it.name.equals(treatmentName, ignoreCase = true)
            })
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfterStopReasonPD() {
        CHEMO_TREATMENT_NAME_STREAM.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTreatment(
                ImmutablePriorTumorTreatment.builder()
                    .name(treatmentName)
                    .isSystemic(true)
                    .startYear(LocalDate.now().minusYears(3).year)
                    .addCategories(TreatmentCategory.CHEMOTHERAPY)
                    .stopReason(PD_STATUS)
                    .build()
            )
            Assert.assertTrue(getTreatmentResultsForPatient(patientRecord).none { it.name.equals(treatmentName, ignoreCase = true) })
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfterBestResponsePD() {
        CHEMO_TREATMENT_NAME_STREAM.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTreatment(
                ImmutablePriorTumorTreatment.builder()
                    .name(treatmentName)
                    .isSystemic(true)
                    .startYear(LocalDate.now().minusYears(3).year)
                    .addCategories(TreatmentCategory.CHEMOTHERAPY)
                    .bestResponse(PD_STATUS)
                    .build()
            )
            Assert.assertTrue(getTreatmentResultsForPatient(patientRecord).none { it.name.equals(treatmentName, ignoreCase = true) })
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfter12Cycles() {
        CHEMO_TREATMENT_NAME_STREAM.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTreatment(
                ImmutablePriorTumorTreatment.builder()
                    .name(treatmentName)
                    .isSystemic(true)
                    .startYear(LocalDate.now().minusYears(3).year)
                    .addCategories(TreatmentCategory.CHEMOTHERAPY)
                    .cycles(12)
                    .build()
            )
            Assert.assertTrue(getTreatmentResultsForPatient(patientRecord).none { it.name.equals(treatmentName, ignoreCase = true) })
        }
    }

    @Test
    fun shouldRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteria() {
        val firstLineChemotherapies = listOf(TreatmentDB.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            getTreatmentResultsForPatient(
                patientRecordWithHistoryAndMolecular(
                    firstLineChemotherapies, emptyList(),
                    TestMolecularFactory.createProperTestMolecularRecord()
                )
            ), 0
        )
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithChemoHistory(firstLineChemotherapies)), 18)
    }

    @Test
    fun shouldNotRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteriaButWithRightSidedTumor() {
        val firstLineChemotherapies = listOf(TreatmentDB.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            getTreatmentResultsForPatient(
                patientRecordWithHistoryAndMolecular(
                    firstLineChemotherapies, emptyList(),
                    MINIMAL_PATIENT_RECORD.molecular(), "Ascending colon"
                )
            ), 0
        )
    }

    private fun assertAntiEGFRTreatmentCount(treatmentResults: List<Treatment>, count: Int) {
        val numMatchingTreatments = treatmentResults.filter { treatment: Treatment ->
            treatment.name.startsWith(TreatmentDB.TREATMENT_CETUXIMAB) || treatment.name.startsWith(TreatmentDB.TREATMENT_PANITUMUMAB)
        }
            .filter { treatment: Treatment -> !treatment.name.contains("Encorafenib") }
            .distinct()
            .count()
        Assert.assertEquals(count, numMatchingTreatments)
    }

    @Test
    fun shouldRecommendPembrolizumabForMSI() {
        Assert.assertFalse(typicalTreatmentResults.any { treatment: Treatment -> treatment.name == TreatmentDB.TREATMENT_PEMBROLIZUMAB })
        val variant: Variant = TestVariantFactory.builder().gene("MLH1").isReportable(true).isBiallelic(true).build()
        val minimal = MINIMAL_PATIENT_RECORD.molecular()
        val molecularRecord: MolecularRecord = ImmutableMolecularRecord.builder()
            .from(minimal)
            .characteristics(
                ImmutableMolecularCharacteristics.builder()
                    .from(minimal.characteristics())
                    .isMicrosatelliteUnstable(true)
                    .build()
            )
            .drivers(ImmutableMolecularDrivers.builder().from(minimal.drivers()).addVariants(variant).build())
            .build()
        Assert.assertTrue(getTreatmentResultsForPatient(
            patientRecordWithHistoryAndMolecular(
                emptyList(), emptyList(),
                molecularRecord
            )
        ).any { treatment: Treatment -> treatment.name == TreatmentDB.TREATMENT_PEMBROLIZUMAB })
    }

    @Test
    fun shouldRecommendCetuximabAndEncorafenibForBRAFV600E() {
        val firstLineChemotherapies = java.util.List.of(TreatmentDB.TREATMENT_CAPOX)
        Assert.assertFalse(getTreatmentResultsForPatient(patientRecordWithChemoHistory(firstLineChemotherapies)).any {
            it.name == "Cetuximab + Encorafenib"
        })
        Assert.assertTrue(getTreatmentResultsForPatient(
            patientRecordWithHistoryAndMolecular(
                firstLineChemotherapies, emptyList(),
                TestMolecularFactory.createProperTestMolecularRecord()
            )
        ).any { it.name == "Cetuximab + Encorafenib" })
    }

    @Test
    fun shouldRecommendLonsurfAfterChemoAndTargetedTherapy() {
        val record = patientRecordWithHistoryAndMolecular(
            listOf(TreatmentDB.TREATMENT_CAPOX), listOf(TreatmentDB.TREATMENT_PANITUMUMAB),
            MINIMAL_PATIENT_RECORD.molecular()
        )
        Assert.assertTrue(getTreatmentResultsForPatient(record).any { it.name == TreatmentDB.TREATMENT_LONSURF })
    }

    @Test
    fun shouldNotRecommendLonsurfAfterTrifluridine() {
        val record = patientRecordWithHistoryAndMolecular(
            listOf(TreatmentDB.TREATMENT_CAPOX, "trifluridine"),
            listOf(TreatmentDB.TREATMENT_PANITUMUMAB), MINIMAL_PATIENT_RECORD.molecular()
        )
        Assert.assertFalse(getTreatmentResultsForPatient(record).any { it.name == TreatmentDB.TREATMENT_LONSURF })
    }

    @Test
    fun shouldThrowExceptionIfPatientDoesNotHaveColorectalCancer() {
        Assert.assertThrows(IllegalArgumentException::class.java) { getTreatmentResultsForPatient(MINIMAL_PATIENT_RECORD) }
    }

    @Test
    fun shouldThrowExceptionIfPatientHasExcludedDoid() {
        listOf("5777", "169", "1800").forEach { doid: String ->
            Assert.assertThrows(IllegalArgumentException::class.java,
                ThrowingRunnable { getTreatmentResultsForPatient(patientRecordWithTumorDoids(doid)) })
        }
    }

    @Test
    fun shouldNotRecommendMultiChemotherapyForPatientsAged75OrOlder() {
        val tumorDetails: TumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build()
        val patientRecord: PatientRecord = ImmutablePatientRecord.copyOf(MINIMAL_PATIENT_RECORD)
            .withClinical(
                ImmutableClinicalRecord.copyOf(MINIMAL_PATIENT_RECORD.clinical())
                    .withTumor(tumorDetails)
                    .withPatient(
                        ImmutablePatientDetails.copyOf(MINIMAL_PATIENT_RECORD.clinical().patient())
                            .withBirthYear(LocalDate.now().minusYears(76).year)
                    )
            )
        assertMultiChemotherapyNotRecommended(patientRecord)
    }

    @Test
    fun shouldNotRecommendMultiChemotherapyForPatientsWithWHOStatusGreaterThan1() {
        val tumorDetails: TumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build()
        val patientRecord: PatientRecord = ImmutablePatientRecord.copyOf(MINIMAL_PATIENT_RECORD)
            .withClinical(
                ImmutableClinicalRecord.copyOf(MINIMAL_PATIENT_RECORD.clinical())
                    .withTumor(tumorDetails)
                    .withClinicalStatus(ImmutableClinicalStatus.copyOf(MINIMAL_PATIENT_RECORD.clinical().clinicalStatus()).withWho(2))
            )
        assertMultiChemotherapyNotRecommended(patientRecord)
    }

    companion object {

        private val CHEMO_TREATMENT_NAME_STREAM = listOf(
            "5-FU",
            "Capecitabine",
            "Irinotecan",
            "Oxaliplatin",
            TreatmentDB.TREATMENT_CAPOX,
            TreatmentDB.TREATMENT_FOLFIRI,
            TreatmentDB.TREATMENT_FOLFIRINOX,
            TreatmentDB.TREATMENT_FOLFOX
        )

        val MINIMAL_PATIENT_RECORD: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
        const val PD_STATUS = "PD"
        private val typicalTreatmentResults: List<Treatment> = getTreatmentResultsForPatient(patientRecord())

        private fun getTreatmentResultsForPatient(patientRecord: PatientRecord): List<Treatment> {
            val doidModel: DoidModel =
                TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer")
            val engine = RecommendationEngine.create(doidModel, ReferenceDateProviderTestFactory.createCurrentDateProvider())
            return engine.determineAvailableTreatments(patientRecord, TreatmentDB.loadTreatments()).map(EvaluatedTreatment::treatment)
        }

        private fun assertSpecificMonotherapyNotRecommended(monotherapy: TreatmentComponent) {
            Assert.assertTrue(typicalTreatmentResults.none { treatment: Treatment ->
                treatment.components.size == 1 && treatment.components.contains(monotherapy)
            })
        }

        private fun assertMultiChemotherapyNotRecommended(patientRecord: PatientRecord) {
            val chemotherapyComponents = java.util.Set.of(
                TreatmentComponent.FLUOROURACIL,
                TreatmentComponent.OXALIPLATIN,
                TreatmentComponent.IRINOTECAN,
                TreatmentComponent.CAPECITABINE
            )
            Assert.assertTrue(getTreatmentResultsForPatient(patientRecord)
                .map { treatment: Treatment -> treatment.components.count { component -> chemotherapyComponents.contains(component) } }
                .none { it > 1 })
        }

        private fun patientRecord(): PatientRecord {
            return patientRecordWithChemoHistory(emptyList())
        }

        private fun patientRecordWithTumorDoids(tumorDoid: String): PatientRecord {
            val tumorDetails: TumorDetails =
                ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID, tumorDoid).build()
            val clinicalRecord: ClinicalRecord =
                ImmutableClinicalRecord.builder().from(MINIMAL_PATIENT_RECORD.clinical()).tumor(tumorDetails).build()
            return ImmutablePatientRecord.builder().from(MINIMAL_PATIENT_RECORD).clinical(clinicalRecord).build()
        }

        private fun patientRecordWithChemoHistory(pastChemotherapyNames: List<String>): PatientRecord {
            return patientRecordWithHistoryAndMolecular(pastChemotherapyNames, emptyList(), MINIMAL_PATIENT_RECORD.molecular())
        }

        private fun patientRecordWithHistoryAndMolecular(
            pastChemotherapyNames: List<String>,
            pastTargetedTherapyNames: List<String>, molecularRecord: MolecularRecord, tumorSubLocation: String? = null
        ): PatientRecord {
            val tumorDetails: TumorDetails = ImmutableTumorDetails.builder()
                .addDoids(DoidConstants.COLORECTAL_CANCER_DOID)
                .primaryTumorSubLocation(tumorSubLocation)
                .build()
            val clinicalRecord: ClinicalRecord = ImmutableClinicalRecord.builder()
                .from(MINIMAL_PATIENT_RECORD.clinical())
                .tumor(tumorDetails)
                .addAllPriorTumorTreatments(priorTreatmentsFromNames(pastChemotherapyNames, TreatmentCategory.CHEMOTHERAPY))
                .addAllPriorTumorTreatments(priorTreatmentsFromNames(pastTargetedTherapyNames, TreatmentCategory.TARGETED_THERAPY))
                .build()
            return PatientRecordFactory.fromInputs(clinicalRecord, molecularRecord)
        }

        private fun patientWithTreatment(treatment: PriorTumorTreatment): ImmutablePatientRecord {
            return ImmutablePatientRecord.copyOf(MINIMAL_PATIENT_RECORD)
                .withClinical(
                    ImmutableClinicalRecord.copyOf(MINIMAL_PATIENT_RECORD.clinical())
                        .withPriorTumorTreatments(treatment)
                        .withTumor(ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build())
                )
        }

        private fun priorTreatmentsFromNames(names: List<String>, category: TreatmentCategory): List<ImmutablePriorTumorTreatment> {
            return names.map { treatmentName: String ->
                ImmutablePriorTumorTreatment.builder()
                    .name(treatmentName)
                    .isSystemic(true)
                    .startYear(LocalDate.now().year)
                    .addCategories(category)
                    .build()
            }
        }
    }
}