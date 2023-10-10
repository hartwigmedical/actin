package com.hartwig.actin.algo.soc

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.medication.AtcTree
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.time.LocalDate

@Ignore
class RecommendationEngineTest {

    @Test
    fun shouldNotRecommendCapecitabineCombinedWithIrinotecan() {
        assertThat(typicalTreatmentResults).noneMatch {
            val drugNames = (it.treatment as Therapy).drugs().map(Drug::name).map(String::uppercase)
            drugNames.contains("CAPECITABINE") && drugNames.contains("IRINOTECAN")
        }
    }

    @Test
    fun shouldNotRecommendOxaliplatinMonotherapy() {
        assertSpecificMonotherapyNotRecommended("OXALIPLATIN")
    }

    @Test
    fun shouldNotRecommendBevacizumabMonotherapy() {
        assertSpecificMonotherapyNotRecommended("BEVACIZUMAB")
    }

    @Test
    fun shouldNotRecommendFolfiriAterCapox() {
        assertThat(getTreatmentResultsForPatient(patientRecordWithChemoHistory(listOf(RecommendationDatabase.TREATMENT_CAPOX)))).noneMatch {
            it.treatment.name().equals(RecommendationDatabase.TREATMENT_FOLFIRI, ignoreCase = true)
        }
    }

    @Test
    fun shouldNotRecommendFolfiriAterFolfox() {
        assertThat(getTreatmentResultsForPatient(patientRecordWithChemoHistory(listOf(RecommendationDatabase.TREATMENT_FOLFOX)))).noneMatch {
            it.treatment.name().equals(RecommendationDatabase.TREATMENT_FOLFIRI, ignoreCase = true)
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfterRecentTreatment() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            assertThat(getTreatmentResultsForPatient(patientRecordWithChemoHistory(listOf(treatmentName)))).noneMatch {
                it.treatment.name().equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfterStopReasonPD() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTherapyNameAndDetails(
                treatmentName,
                ImmutableTherapyHistoryDetails.builder().stopReason(StopReason.PROGRESSIVE_DISEASE).build()
            )
            assertThat(getTreatmentResultsForPatient(patientRecord)).noneMatch {
                it.treatment.name().equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfterBestResponsePD() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTherapyNameAndDetails(
                treatmentName,
                ImmutableTherapyHistoryDetails.builder().bestResponse(TreatmentResponse.PROGRESSIVE_DISEASE).build()
            )
            assertThat(getTreatmentResultsForPatient(patientRecord)).noneMatch {
                it.treatment.name().equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun shouldNotRecommendTheSameChemotherapyAfter12Cycles() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTherapyNameAndDetails(
                treatmentName,
                ImmutableTherapyHistoryDetails.builder().cycles(12).build()
            )
            assertThat(getTreatmentResultsForPatient(patientRecord)).noneMatch {
                it.treatment.name().equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun shouldRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteria() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            getTreatmentResultsForPatient(
                patientRecordWithHistoryAndMolecular(
                    firstLineChemotherapies, TestMolecularFactory.createProperTestMolecularRecord()
                )
            ), 0
        )
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithChemoHistory(firstLineChemotherapies)), 18)
    }

    @Test
    fun shouldNotRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteriaButWithRightSidedTumor() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            getTreatmentResultsForPatient(
                patientRecordWithHistoryAndMolecular(
                    firstLineChemotherapies, MINIMAL_PATIENT_RECORD.molecular(),
                    "Ascending colon"
                )
            ), 0
        )
    }

    private fun assertAntiEGFRTreatmentCount(treatmentResults: List<TreatmentCandidate>, count: Int) {
        val matchingTreatments = treatmentResults.filter { candidate ->
            val drugNames = (candidate.treatment as Therapy).drugs().map(Drug::name).map(String::uppercase)
            drugNames.any { it == RecommendationDatabase.TREATMENT_CETUXIMAB || it == RecommendationDatabase.TREATMENT_PANITUMUMAB } && drugNames.none { it == "ENCORAFENIB" }
        }.distinct()

        assertThat(matchingTreatments).hasSize(count)
    }

    @Test
    fun shouldRecommendPembrolizumabForMSI() {
        assertThat(typicalTreatmentResults).noneMatch { it.treatment.name() == RecommendationDatabase.TREATMENT_PEMBROLIZUMAB }
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
        assertThat(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(emptyList(), molecularRecord))).anyMatch {
            it.treatment.name() == RecommendationDatabase.TREATMENT_PEMBROLIZUMAB
        }
    }

    @Test
    fun shouldRecommendCetuximabAndEncorafenibForBRAFV600E() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        assertThat(getTreatmentResultsForPatient(patientRecordWithChemoHistory(firstLineChemotherapies))).noneMatch {
            it.treatment.name().uppercase() == "CETUXIMAB+ENCORAFENIB"
        }
        assertThat(
            getTreatmentResultsForPatient(
                patientRecordWithHistoryAndMolecular(
                    firstLineChemotherapies, TestMolecularFactory.createProperTestMolecularRecord()
                )
            )
        ).anyMatch { it.treatment.name() == "CETUXIMAB+ENCORAFENIB" }
    }

    @Test
    fun shouldRecommendLonsurfAfterChemoAndTargetedTherapy() {
        val record = patientRecordWithHistoryAndMolecular(
            listOf(RecommendationDatabase.TREATMENT_CAPOX, RecommendationDatabase.TREATMENT_PANITUMUMAB), MINIMAL_PATIENT_RECORD.molecular()
        )
        assertThat(getTreatmentResultsForPatient(record)).anyMatch {
            treatmentCandidateMatchesName(
                it,
                RecommendationDatabase.TREATMENT_LONSURF
            )
        }
    }

    @Test
    fun shouldNotRecommendLonsurfAfterTrifluridine() {
        val record = patientRecordWithHistoryAndMolecular(
            listOf(RecommendationDatabase.TREATMENT_CAPOX, "trifluridine", RecommendationDatabase.TREATMENT_PANITUMUMAB),
            MINIMAL_PATIENT_RECORD.molecular()
        )
        assertThat(getTreatmentResultsForPatient(record)).noneMatch {
            treatmentCandidateMatchesName(
                it,
                RecommendationDatabase.TREATMENT_LONSURF
            )
        }
    }

    @Test
    fun shouldThrowExceptionIfPatientDoesNotHaveColorectalCancer() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { getTreatmentResultsForPatient(MINIMAL_PATIENT_RECORD) }
    }

    @Test
    fun shouldThrowExceptionIfPatientHasExcludedDoid() {
        listOf("5777", "169", "1800").forEach { doid: String ->
            assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
                getTreatmentResultsForPatient(patientRecordWithTumorDoids(doid))
            }
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
        private val ACTIN_RESOURCE_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "crunch-resources-private",
            "actin"
        ).joinToString(File.separator)

        private val TREATMENT_JSON_PATH = ACTIN_RESOURCE_PATH + File.separator + "treatment_db"

        private val TREATMENT_DATABASE = TreatmentDatabaseFactory.createFromPath(TREATMENT_JSON_PATH)

        private val ATC_TREE =
            AtcTree.createFromFile(listOf(ACTIN_RESOURCE_PATH, "atc_config", "atc_tree.tsv").joinToString(File.separator))

        private val CHEMO_TREATMENT_NAMES = listOf(
            "5-FU",
            "CAPECITABINE",
            "IRINOTECAN",
            "OXALIPLATIN",
            RecommendationDatabase.TREATMENT_CAPOX,
            RecommendationDatabase.TREATMENT_FOLFIRI,
            RecommendationDatabase.TREATMENT_FOLFIRINOX,
            RecommendationDatabase.TREATMENT_FOLFOX
        )

        private val MINIMAL_PATIENT_RECORD: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
        private val typicalTreatmentResults: List<TreatmentCandidate> = getTreatmentResultsForPatient(patientRecord())

        private fun getTreatmentResultsForPatient(patientRecord: PatientRecord): List<TreatmentCandidate> {
            val doidModel: DoidModel =
                TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer")
            val engine = RecommendationEngine.create(
                doidModel, ATC_TREE, RecommendationDatabase(TREATMENT_DATABASE),
                ReferenceDateProviderTestFactory.createCurrentDateProvider()
            )
            return engine.determineAvailableTreatments(patientRecord).map(EvaluatedTreatment::treatmentCandidate)
        }

        private fun assertSpecificMonotherapyNotRecommended(drugName: String) {
            assertThat(typicalTreatmentResults).noneMatch { treatmentCandidate: TreatmentCandidate ->
                val drugs = (treatmentCandidate.treatment as Therapy).drugs()
                drugs.size == 1 && drugs.map(Drug::name).any { it.equals(drugName, ignoreCase = true) }
            }
        }

        private fun assertMultiChemotherapyNotRecommended(patientRecord: PatientRecord) {
            val chemotherapyComponents = setOf("Fluorouracil", "Capecitabine", "Irinotecan", "Oxaliplatin")
            assertThat(
                getTreatmentResultsForPatient(patientRecord)
                    .map { treatmentCandidate ->
                        (treatmentCandidate.treatment as Therapy).drugs()
                            .count { chemotherapyComponents.contains(it.name()) }
                    })
                .noneMatch { it > 1 }
        }

        private fun treatmentCandidateMatchesName(treatmentCandidate: TreatmentCandidate, name: String): Boolean {
            return treatmentCandidate.treatment.name().equals(name, ignoreCase = true) || treatmentCandidate.treatment.synonyms().any {
                it.equals(name, ignoreCase = true)
            }
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
            return patientRecordWithHistoryAndMolecular(pastChemotherapyNames, MINIMAL_PATIENT_RECORD.molecular())
        }

        private fun patientRecordWithHistoryAndMolecular(
            pastTreatmentNames: List<String>,
            molecularRecord: MolecularRecord, tumorSubLocation: String? = null
        ): PatientRecord {
            val tumorDetails: TumorDetails = ImmutableTumorDetails.builder()
                .addDoids(DoidConstants.COLORECTAL_CANCER_DOID)
                .primaryTumorSubLocation(tumorSubLocation)
                .build()
            val clinicalRecord: ClinicalRecord = ImmutableClinicalRecord.builder()
                .from(MINIMAL_PATIENT_RECORD.clinical())
                .tumor(tumorDetails)
                .addAllTreatmentHistory(treatmentHistoryFromNames(pastTreatmentNames))
                .build()
            return PatientRecordFactory.fromInputs(clinicalRecord, molecularRecord)
        }

        private fun patientWithTherapyNameAndDetails(therapyName: String, details: TherapyHistoryDetails): ImmutablePatientRecord {
            val treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(TREATMENT_DATABASE.findTreatmentByName(therapyName)!!)
                .startYear(LocalDate.now().minusYears(3).year)
                .therapyHistoryDetails(details)
                .build()

            return ImmutablePatientRecord.copyOf(MINIMAL_PATIENT_RECORD)
                .withClinical(
                    ImmutableClinicalRecord.copyOf(MINIMAL_PATIENT_RECORD.clinical())
                        .withTreatmentHistory(setOf(treatmentHistoryEntry))
                        .withTumor(ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build())
                )
        }

        private fun treatmentHistoryFromNames(names: List<String>): List<ImmutableTreatmentHistoryEntry> {
            return names.map { treatmentName: String ->
                ImmutableTreatmentHistoryEntry.builder()
                    .treatments(setOf(TREATMENT_DATABASE.findTreatmentByName(treatmentName)!!))
                    .startYear(LocalDate.now().year)
                    .build()
            }
        }
    }
}