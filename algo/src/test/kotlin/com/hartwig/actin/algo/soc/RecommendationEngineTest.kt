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
    fun `Should not recommend Capecitabine combined with Irinotecan`() {
        assertThat(typicalTreatmentResults).noneMatch {
            val drugNames = (it.treatment as Therapy).drugs().map(Drug::name).map(String::uppercase)
            drugNames.contains("CAPECITABINE") && drugNames.contains("IRINOTECAN")
        }
    }

    @Test
    fun `Should not recommend Oxaliplatin monotherapy`() {
        assertSpecificTreatmentNotRecommended("OXALIPLATIN")
    }

    @Test
    fun `Should not recommend Bevacizumab monotherapy`() {
        assertSpecificTreatmentNotRecommended("BEVACIZUMAB")
    }

    @Test
    fun `Should not recommend CAPOXIRI`() {
        assertSpecificTreatmentNotRecommended("CAPOXIRI")
    }

    @Test
    fun `Should not recommend FOLFIRI after CAPOX`() {
        assertThat(getTreatmentResultsForPatient(patientRecordWithTreatmentHistory(listOf(RecommendationDatabase.TREATMENT_CAPOX)))).noneMatch {
            it.treatment.name().equals(RecommendationDatabase.TREATMENT_FOLFIRI, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend FOLFIRI after FOLFOX`() {
        assertThat(getTreatmentResultsForPatient(patientRecordWithTreatmentHistory(listOf(RecommendationDatabase.TREATMENT_FOLFOX)))).noneMatch {
            it.treatment.name().equals(RecommendationDatabase.TREATMENT_FOLFIRI, ignoreCase = true)
        }
    }

    @Test
    fun `Should recommend FOLFOXIRI+Bevacizumab first for fit patients`() {
        assertThat(typicalTreatmentResults.first().treatment.name()).isEqualTo("${RecommendationDatabase.TREATMENT_FOLFOXIRI}+BEVACIZUMAB")
    }

    @Test
    fun `Should recommend Irinotecan monotherapy in second line after first-line Oxaliplatin treatment`() {
        assertSpecificTreatmentNotRecommended(RecommendationDatabase.TREATMENT_IRINOTECAN)
        assertThat(
            getTreatmentResultsForPatient(
                patientRecordWithTreatmentHistory(
                    listOf(
                        RecommendationDatabase.TREATMENT_CAPOX,
                    )
                )
            )
        ).anyMatch { it.treatment.name() == RecommendationDatabase.TREATMENT_IRINOTECAN }
    }

    @Test
    fun `Should require Oxaliplatin or Irinotecan for SOC exhaustion`() {
        val pastTreatmentNames = listOf(
            RecommendationDatabase.TREATMENT_PEMBROLIZUMAB,
            "CAPECITABINE+BEVACIZUMAB",
            "CETUXIMAB",
            RecommendationDatabase.TREATMENT_LONSURF
        )
        val patientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames)
        assertThat(getTreatmentResultsForPatient(patientRecord).map { it.treatment.name() }.toSet()).contains(
            RecommendationDatabase.TREATMENT_CAPOX,
            RecommendationDatabase.TREATMENT_IRINOTECAN,
            RecommendationDatabase.TREATMENT_FOLFOX
        )
        assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(patientRecord)).isFalse

        listOf(RecommendationDatabase.TREATMENT_CAPOX, RecommendationDatabase.TREATMENT_IRINOTECAN).forEach { treatment ->
            val updatedPatientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames + treatment)
            assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(updatedPatientRecord)).isTrue()
        }
    }

    @Test
    fun `Should require 5-FU or Capecitabine for SOC exhaustion`() {
        val pastTreatmentNames = listOf(
            RecommendationDatabase.TREATMENT_PEMBROLIZUMAB,
            "OXALIPLATIN+BEVACIZUMAB",
            "CETUXIMAB",
            RecommendationDatabase.TREATMENT_LONSURF
        )
        val patientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames)
        assertThat(getTreatmentResultsForPatient(patientRecord).map { it.treatment.name() }.toSet()).contains(
            "CAPECITABINE", "FLUOROURACIL", RecommendationDatabase.TREATMENT_FOLFOXIRI
        )
        assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(patientRecord)).isFalse

        listOf("CAPECITABINE", "FLUOROURACIL").forEach { treatment ->
            val updatedPatientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames + treatment)
            assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(updatedPatientRecord)).isTrue()
        }
    }

    @Test
    fun `Should not recommend the same chemotherapy after recent treatment`() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            assertThat(getTreatmentResultsForPatient(patientRecordWithTreatmentHistory(listOf(treatmentName)))).noneMatch {
                it.treatment.name().equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should not recommend the same chemotherapy after stop reason PD`() {
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
    fun `Should not recommend the same chemotherapy after best response PD`() {
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
    fun `Should not recommend the same chemotherapy after 12 cycles`() {
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
    fun `Should recommend second-line anti-EGFR therapy for patients matching molecular criteria`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            resultsForPatientWithHistoryAndMolecular(
                firstLineChemotherapies, TestMolecularFactory.createProperTestMolecularRecord()
            ), 0
        )
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithTreatmentHistory(firstLineChemotherapies)), 12)
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria but with right sided tumor`() {
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

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria who have already received anti-EGFR monotherapy`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        listOf(RecommendationDatabase.TREATMENT_CETUXIMAB, RecommendationDatabase.TREATMENT_PANITUMUMAB).forEach {
            val patientRecord = patientRecordWithTreatmentHistory(firstLineChemotherapies + it)
            assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecord), 0)
        }
    }

    private fun assertAntiEGFRTreatmentCount(treatmentResults: List<TreatmentCandidate>, count: Int) {
        val matchingTreatments = treatmentResults.filter { candidate ->
            val drugNames = (candidate.treatment as Therapy).drugs().map(Drug::name).map(String::uppercase)
            drugNames.any { it == RecommendationDatabase.TREATMENT_CETUXIMAB || it == RecommendationDatabase.TREATMENT_PANITUMUMAB } && drugNames.none { it == "ENCORAFENIB" }
        }.distinct()

        assertThat(matchingTreatments).hasSize(count)
    }

    @Test
    fun `Should not combine anti-EGFR therapy with capecitabine`() {
        val antiEGFRTherapies = antiEGFRTherapies()
        assertThat(antiEGFRTherapies).isNotEmpty
        assertThat(antiEGFRTherapies).noneMatch { it.drugs().any { drug -> drug.name().uppercase() == "CAPECITABINE" } }
    }

    @Test
    fun `Should not combine anti-EGFR therapy with FOLFOXIRI`() {
        val antiEGFRTherapies = antiEGFRTherapies()
        assertThat(antiEGFRTherapies).isNotEmpty
        listOf(RecommendationDatabase.TREATMENT_CETUXIMAB, RecommendationDatabase.TREATMENT_PANITUMUMAB).forEach {
            assertThat(antiEGFRTherapies).noneMatch { it.name().equals("FOLFOXIRI+$it", ignoreCase = true) }
        }
    }

    private fun antiEGFRTherapies(): List<Therapy> {
        return getTreatmentResultsForPatient(patientRecordWithTreatmentHistory(listOf(RecommendationDatabase.TREATMENT_CAPOX)))
            .mapNotNull { it.treatment as? Therapy }
            .filter {
                it.drugs().any { drug ->
                    drug.name().uppercase() == RecommendationDatabase.TREATMENT_CETUXIMAB ||
                            drug.name().uppercase() == RecommendationDatabase.TREATMENT_PANITUMUMAB
                }
            }
    }

    @Test
    fun `Should recommend Pembrolizumab first in first line for patients with MSI`() {
        assertFirstResultIsPembrolizumabForPatientWithTreatmentHistoryAndMSI(emptyList())
    }

    @Test
    fun `Should recommend Pembrolizumab first in second line for patients with MSI`() {
        assertFirstResultIsPembrolizumabForPatientWithTreatmentHistoryAndMSI(listOf(RecommendationDatabase.TREATMENT_FOLFOX))
    }

    private fun assertFirstResultIsPembrolizumabForPatientWithTreatmentHistoryAndMSI(treatmentHistory: List<String>) {
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
        val firstResult = getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(treatmentHistory, molecularRecord)).first()
        assertThat(firstResult.isOptional).isFalse
        assertThat(firstResult.treatment.name()).isEqualTo(RecommendationDatabase.TREATMENT_PEMBROLIZUMAB)
    }

    @Test
    fun `Should recommend Cetuximab+Encorafenib after first line for patient with BRAF V600E`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        val cetuximabAndEncorafenib = "CETUXIMAB+ENCORAFENIB"
        assertThat(getTreatmentResultsForPatient(patientRecordWithTreatmentHistory(firstLineChemotherapies)))
            .noneMatch { it.treatment.name().uppercase() == cetuximabAndEncorafenib }

        val molecularRecordWithBRAFV600E = TestMolecularFactory.createProperTestMolecularRecord()
        assertThat(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(emptyList(), molecularRecordWithBRAFV600E)))
            .noneMatch { it.treatment.name().uppercase() == cetuximabAndEncorafenib }

        assertThat(
            getTreatmentResultsForPatient(
                patientRecordWithHistoryAndMolecular(firstLineChemotherapies, molecularRecordWithBRAFV600E)
            )
        ).anyMatch { it.treatment.name() == cetuximabAndEncorafenib }
    }

    @Test
    fun `Should recommend optional Lonsurf after chemo and targeted therapy`() {
        val result = resultsForPatientWithHistoryAndMolecular(
            listOf(RecommendationDatabase.TREATMENT_CAPOX, RecommendationDatabase.TREATMENT_PANITUMUMAB), MINIMAL_PATIENT_RECORD.molecular()
        ).find { treatmentCandidateMatchesName(it, RecommendationDatabase.TREATMENT_LONSURF) }
        assertThat(result).isNotNull
        assertThat(result?.isOptional).isTrue
    }

    @Test
    fun `Should not recommend Lonsurf after Trifluridine`() {
        val results = resultsForPatientWithHistoryAndMolecular(
            listOf(RecommendationDatabase.TREATMENT_CAPOX, "trifluridine", RecommendationDatabase.TREATMENT_PANITUMUMAB),
            MINIMAL_PATIENT_RECORD.molecular()
        )
        assertThat(results).noneMatch { treatmentCandidateMatchesName(it, RecommendationDatabase.TREATMENT_LONSURF) }
    }

    @Test
    fun `Should throw exception if patient does not have colorectal cancer`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { getTreatmentResultsForPatient(MINIMAL_PATIENT_RECORD) }
    }

    @Test
    fun `Should throw exception if patient has excluded DOID`() {
        listOf("5777", "169", "1800").forEach { doid: String ->
            assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
                getTreatmentResultsForPatient(patientRecordWithTumorDoids(doid))
            }
        }
    }

    @Test
    fun `Should not recommend multi-chemotherapy for patients aged 75 or older`() {
        assertMultiChemotherapyNotRecommended(OLDER_PATIENT)
    }

    @Test
    fun `Should not recommend multi chemotherapy for patients with WHO status greater than 1`() {
        assertMultiChemotherapyNotRecommended(PATIENT_WITH_HIGH_WHO_STATUS)
    }

    @Test
    fun `Should recommend fluoropyrimidine+Bevacizumab treatments for patients unfit to receive combination chemotherapy`() {
        val expectedTreatmentNames = setOf("FLUOROURACIL+BEVACIZUMAB", "CAPECITABINE+BEVACIZUMAB")
        listOf(OLDER_PATIENT, PATIENT_WITH_HIGH_WHO_STATUS).forEach { patient ->
            val results = getTreatmentResultsForPatient(patient)
            assertThat(results.filter { it.treatment.name() in expectedTreatmentNames }).hasSize(2)
        }
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

        private val RECOMMENDATION_ENGINE = RecommendationEngine.create(
            TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer"),
            AtcTree.createFromFile(listOf(ACTIN_RESOURCE_PATH, "atc_config", "atc_tree.tsv").joinToString(File.separator)),
            RecommendationDatabase(TREATMENT_DATABASE),
            ReferenceDateProviderTestFactory.createCurrentDateProvider()
        )

        private val CHEMO_TREATMENT_NAMES = listOf(
            "5-FU",
            "CAPECITABINE",
            "IRINOTECAN",
            "OXALIPLATIN",
            RecommendationDatabase.TREATMENT_CAPOX,
            RecommendationDatabase.TREATMENT_FOLFIRI,
            RecommendationDatabase.TREATMENT_FOLFOXIRI,
            RecommendationDatabase.TREATMENT_FOLFOX
        )

        private val MINIMAL_PATIENT_RECORD: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
        private val MINIMAL_CRC_PATIENT_RECORD = ImmutablePatientRecord.copyOf(MINIMAL_PATIENT_RECORD).withClinical(
            ImmutableClinicalRecord.copyOf(MINIMAL_PATIENT_RECORD.clinical())
                .withTumor(ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build())
        )
        private val OLDER_PATIENT: PatientRecord = ImmutablePatientRecord.copyOf(MINIMAL_CRC_PATIENT_RECORD).withClinical(
            ImmutableClinicalRecord.copyOf(MINIMAL_CRC_PATIENT_RECORD.clinical()).withPatient(
                ImmutablePatientDetails.copyOf(MINIMAL_CRC_PATIENT_RECORD.clinical().patient())
                    .withBirthYear(LocalDate.now().minusYears(76).year)
            )
        )
        private val PATIENT_WITH_HIGH_WHO_STATUS = ImmutablePatientRecord.copyOf(MINIMAL_CRC_PATIENT_RECORD).withClinical(
            ImmutableClinicalRecord.copyOf(MINIMAL_CRC_PATIENT_RECORD.clinical())
                .withClinicalStatus(ImmutableClinicalStatus.copyOf(MINIMAL_CRC_PATIENT_RECORD.clinical().clinicalStatus()).withWho(2))
        )

        private val typicalTreatmentResults: List<TreatmentCandidate> = getTreatmentResultsForPatient(patientRecord())

        private fun getTreatmentResultsForPatient(patientRecord: PatientRecord): List<TreatmentCandidate> {
            return RECOMMENDATION_ENGINE.determineAvailableTreatments(patientRecord).map(EvaluatedTreatment::treatmentCandidate)
        }

        private fun resultsForPatientWithHistoryAndMolecular(
            pastTreatmentNames: List<String>,
            molecularRecord: MolecularRecord
        ): List<TreatmentCandidate> {
            val patientRecord: PatientRecord = patientRecordWithHistoryAndMolecular(pastTreatmentNames, molecularRecord)
            return getTreatmentResultsForPatient(patientRecord)
        }

        private fun assertSpecificTreatmentNotRecommended(name: String) {
            assertThat(typicalTreatmentResults).noneMatch {
                it.treatment.name().equals(name, ignoreCase = true) || it.treatment.synonyms().contains(name.uppercase())
            }
        }

        private fun assertMultiChemotherapyNotRecommended(patientRecord: PatientRecord) {
            val chemotherapyComponents = setOf("FLUOROURACIL", "CAPECITABINE", "IRINOTECAN", "OXALIPLATIN")
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
            return patientRecordWithTreatmentHistory(emptyList())
        }

        private fun patientRecordWithTumorDoids(tumorDoid: String): PatientRecord {
            val tumorDetails: TumorDetails =
                ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID, tumorDoid).build()
            val clinicalRecord: ClinicalRecord =
                ImmutableClinicalRecord.builder().from(MINIMAL_PATIENT_RECORD.clinical()).tumor(tumorDetails).build()
            return ImmutablePatientRecord.builder().from(MINIMAL_PATIENT_RECORD).clinical(clinicalRecord).build()
        }

        private fun patientRecordWithTreatmentHistory(pastTreatmentNames: List<String>): PatientRecord {
            return patientRecordWithHistoryAndMolecular(pastTreatmentNames, MINIMAL_PATIENT_RECORD.molecular())
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

            return ImmutablePatientRecord.copyOf(MINIMAL_CRC_PATIENT_RECORD).withClinical(
                ImmutableClinicalRecord.copyOf(MINIMAL_CRC_PATIENT_RECORD.clinical()).withTreatmentHistory(setOf(treatmentHistoryEntry))
            )
        }

        private fun treatmentHistoryFromNames(names: List<String>): List<ImmutableTreatmentHistoryEntry> {
            val treatmentDate = LocalDate.now().minusMonths(4)
            return names.map { treatmentName: String ->
                ImmutableTreatmentHistoryEntry.builder()
                    .treatments(setOf(TREATMENT_DATABASE.findTreatmentByName(treatmentName)!!))
                    .startYear(treatmentDate.year)
                    .startMonth(treatmentDate.monthValue)
                    .build()
            }
        }
    }
}