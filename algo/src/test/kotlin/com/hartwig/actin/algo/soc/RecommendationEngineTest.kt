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
        assertThat(TYPICAL_TREATMENT_RESULTS).noneMatch {
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
    fun `Should not recommend FOLFOX after CAPOX`() {
        assertThat(resultsForPatientWithHistory(listOf(RecommendationDatabase.TREATMENT_CAPOX))).noneMatch {
            it.treatment.name().equals(RecommendationDatabase.TREATMENT_FOLFOX, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend CAPOX after FOLFOX`() {
        assertThat(resultsForPatientWithHistory(listOf(RecommendationDatabase.TREATMENT_FOLFOX))).noneMatch {
            it.treatment.name().equals(RecommendationDatabase.TREATMENT_CAPOX, ignoreCase = true)
        }
    }

    @Test
    fun `Should recommend FOLFOXIRI+Bevacizumab first for fit patients that are not eligible for anti-EGFR or Pembrolizumab`() {
        val results = resultsForPatientWithHistoryAndMolecular(emptyList(), MOLECULAR_RECORD_WITH_BRAF_V600E)
        assertThat(results.first().treatment.name()).isEqualTo("${RecommendationDatabase.TREATMENT_FOLFOXIRI}+BEVACIZUMAB")
    }

    @Test
    fun `Should recommend Irinotecan monotherapy in second line after first-line Oxaliplatin treatment`() {
        assertSpecificTreatmentNotRecommended(RecommendationDatabase.TREATMENT_IRINOTECAN)
        assertThat(resultsForPatientWithHistory(listOf(RecommendationDatabase.TREATMENT_CAPOX)))
            .anyMatch { it.treatment.name() == RecommendationDatabase.TREATMENT_IRINOTECAN }
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
        assertThat(resultsForPatient(patientRecord).map { it.treatment.name() }.toSet()).contains(
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
        assertThat(resultsForPatient(patientRecord).map { it.treatment.name() }.toSet()).contains(
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
            assertThat(resultsForPatientWithHistory(listOf(treatmentName))).noneMatch {
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
            assertThat(resultsForPatient(patientRecord)).noneMatch {
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
            assertThat(resultsForPatient(patientRecord)).noneMatch {
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
            assertThat(resultsForPatient(patientRecord)).noneMatch {
                it.treatment.name().equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should recommend first-line anti-EGFR+chemotherapy first for patients matching molecular criteria`() {
        assertAntiEGFRTreatmentCount(resultsForPatientWithHistoryAndMolecular(emptyList(), MOLECULAR_RECORD_WITH_BRAF_V600E), 0)
        assertAntiEGFRTreatmentCount(TYPICAL_TREATMENT_RESULTS, 10)

        assertThat(TYPICAL_TREATMENT_RESULTS.take(MULTICHEMOTHERAPIES_WITH_EGFR.size).map { it.treatment.name() })
            .containsAll(MULTICHEMOTHERAPIES_WITH_EGFR)
    }

    @Test
    fun `Should recommend second-line anti-EGFR therapy first for patients matching molecular criteria`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            resultsForPatientWithHistoryAndMolecular(firstLineChemotherapies, MOLECULAR_RECORD_WITH_BRAF_V600E), 0
        )

        val results = resultsForPatientWithHistory(firstLineChemotherapies)
        assertAntiEGFRTreatmentCount(results, 12)

        assertThat(results.take(MULTICHEMOTHERAPIES_WITH_EGFR.size).map { it.treatment.name() })
            .containsAll(MULTICHEMOTHERAPIES_WITH_EGFR)
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria but with right sided tumor`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        assertAntiEGFRTreatmentCount(
            resultsForPatientWithHistoryAndMolecular(firstLineChemotherapies, MINIMAL_PATIENT_RECORD.molecular(), "Ascending colon"), 0
        )
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria who have already received anti-EGFR monotherapy`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        listOf(RecommendationDatabase.TREATMENT_CETUXIMAB, RecommendationDatabase.TREATMENT_PANITUMUMAB).forEach {
            assertAntiEGFRTreatmentCount(resultsForPatientWithHistory(firstLineChemotherapies + it), 0)
        }
    }

    private fun assertAntiEGFRTreatmentCount(treatmentResults: List<TreatmentCandidate>, count: Int) {
        val matchingTreatments = treatmentResults.filter { candidate ->
            val drugNames = (candidate.treatment as Therapy).drugs().map(Drug::name).map(String::uppercase)
            drugNames.any { it in RecommendationDatabase.EGFR_TREATMENTS } && drugNames.none { it == "ENCORAFENIB" }
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
        RecommendationDatabase.EGFR_TREATMENTS.forEach { antiEGFRTreatment ->
            assertThat(antiEGFRTherapies).noneMatch { it.name().equals("FOLFOXIRI+$antiEGFRTreatment", ignoreCase = true) }
        }
    }

    private fun antiEGFRTherapies(): List<Therapy> {
        return resultsForPatientWithHistory(listOf(RecommendationDatabase.TREATMENT_CAPOX))
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
        assertThat(TYPICAL_TREATMENT_RESULTS).noneMatch { it.treatment.name() == RecommendationDatabase.TREATMENT_PEMBROLIZUMAB }

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

        val firstResult = resultsForPatientWithHistoryAndMolecular(treatmentHistory, molecularRecord).first()
        assertThat(firstResult.isOptional).isFalse
        assertThat(firstResult.treatment.name()).isEqualTo(RecommendationDatabase.TREATMENT_PEMBROLIZUMAB)
    }

    @Test
    fun `Should recommend Cetuximab+Encorafenib after first line for patient with BRAF V600E`() {
        val firstLineChemotherapies = listOf(RecommendationDatabase.TREATMENT_CAPOX)
        val cetuximabAndEncorafenib = "CETUXIMAB+ENCORAFENIB"
        assertThat(resultsForPatientWithHistory(firstLineChemotherapies))
            .noneMatch { it.treatment.name().uppercase() == cetuximabAndEncorafenib }

        assertThat(resultsForPatientWithHistoryAndMolecular(emptyList(), MOLECULAR_RECORD_WITH_BRAF_V600E))
            .noneMatch { it.treatment.name().uppercase() == cetuximabAndEncorafenib }

        assertThat(resultsForPatientWithHistoryAndMolecular(firstLineChemotherapies, MOLECULAR_RECORD_WITH_BRAF_V600E))
            .anyMatch { it.treatment.name() == cetuximabAndEncorafenib }
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
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { resultsForPatient(MINIMAL_PATIENT_RECORD) }
    }

    @Test
    fun `Should throw exception if patient has excluded DOID`() {
        listOf("5777", "169", "1800").forEach { doid: String ->
            assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
                resultsForPatient(patientRecordWithTumorDoids(doid))
            }
        }
    }

    @Test
    fun `Should not recommend multi-chemotherapy for patients aged 75 or older`() {
        assertMultiChemotherapyNotRecommended(OLDER_PATIENT)
    }

    @Test
    fun `Should not recommend multi chemotherapy for patients with WHO status greater than 2`() {
        assertMultiChemotherapyNotRecommended(PATIENT_WITH_HIGH_WHO_STATUS)
    }

    @Test
    fun `Should recommend fluoropyrimidine+Bevacizumab treatments for patients unfit to receive combination chemotherapy`() {
        val expectedTreatmentNames = setOf("FLUOROURACIL+BEVACIZUMAB", "CAPECITABINE+BEVACIZUMAB")
        listOf(OLDER_PATIENT, PATIENT_WITH_HIGH_WHO_STATUS).forEach { patient ->
            val results = resultsForPatient(patient)
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

        private val MULTICHEMOTHERAPIES_WITH_EGFR =
            listOf(RecommendationDatabase.TREATMENT_FOLFOX, RecommendationDatabase.TREATMENT_FOLFIRI)
                .flatMap { chemo -> RecommendationDatabase.EGFR_TREATMENTS.map { "$chemo+$it" } }


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
                .withClinicalStatus(ImmutableClinicalStatus.copyOf(MINIMAL_CRC_PATIENT_RECORD.clinical().clinicalStatus()).withWho(3))
        )

        private val MOLECULAR_RECORD_WITH_BRAF_V600E = TestMolecularFactory.createProperTestMolecularRecord()

        private val TYPICAL_TREATMENT_RESULTS: List<TreatmentCandidate> =
            resultsForPatientWithHistory(emptyList())

        private fun resultsForPatient(patientRecord: PatientRecord): List<TreatmentCandidate> {
            return RECOMMENDATION_ENGINE.determineAvailableTreatments(patientRecord).map(EvaluatedTreatment::treatmentCandidate)
        }

        private fun resultsForPatientWithHistory(pastTreatmentNames: List<String>): List<TreatmentCandidate> {
            return resultsForPatient(patientRecordWithTreatmentHistory(pastTreatmentNames))
        }

        private fun resultsForPatientWithHistoryAndMolecular(
            pastTreatmentNames: List<String>, molecularRecord: MolecularRecord, tumorSubLocation: String? = null
        ): List<TreatmentCandidate> {
            return resultsForPatient(patientRecordWithHistoryAndMolecular(pastTreatmentNames, molecularRecord, tumorSubLocation))
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

        private fun assertSpecificTreatmentNotRecommended(name: String) {
            assertThat(TYPICAL_TREATMENT_RESULTS).noneMatch { treatmentCandidateMatchesName(it, name) }
        }

        private fun assertMultiChemotherapyNotRecommended(patientRecord: PatientRecord) {
            val chemotherapyComponents = setOf("FLUOROURACIL", "CAPECITABINE", "IRINOTECAN", "OXALIPLATIN")
            assertThat(
                resultsForPatient(patientRecord)
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

        private fun patientRecordWithTumorDoids(tumorDoid: String): PatientRecord {
            val tumorDetails: TumorDetails =
                ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID, tumorDoid).build()
            val clinicalRecord: ClinicalRecord =
                ImmutableClinicalRecord.builder().from(MINIMAL_PATIENT_RECORD.clinical()).tumor(tumorDetails).build()
            return ImmutablePatientRecord.builder().from(MINIMAL_PATIENT_RECORD).clinical(clinicalRecord).build()
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