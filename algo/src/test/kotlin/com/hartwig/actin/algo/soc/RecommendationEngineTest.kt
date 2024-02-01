package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.medication.AtcTree
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import java.io.File
import java.time.LocalDate

class RecommendationEngineTest {

    @Test
    fun `Should not recommend Oxaliplatin monotherapy`() {
        assertSpecificTreatmentNotRecommended("OXALIPLATIN")
    }

    @Test
    fun `Should not recommend Bevacizumab monotherapy`() {
        assertSpecificTreatmentNotRecommended("BEVACIZUMAB")
    }

    @Test
    fun `Should not recommend Oxaliplatin+Bevacizumab`() {
        // Oxaliplatin should always be given in combination with 5-FU or Capecitabine
        // TODO
    }

    @Test
    fun `Should not recommend Irinotecan+Bevacizumab`() {
        // This is never given in the Netherlands
        // TODO
    }

    @Test
    fun `Should not recommend CAPOXIRI`() {
        assertSpecificTreatmentNotRecommended("CAPOXIRI")
    }

    @Test
    fun `Should not recommend FOLFOX after CAPOX`() {
        assertThat(resultsForPatientWithHistory(listOf(CAPOX))).noneMatch {
            it.treatment.name.equals(FOLFOX, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend CAPOX after FOLFOX`() {
        assertThat(resultsForPatientWithHistory(listOf(FOLFOX))).noneMatch {
            it.treatment.name.equals(CAPOX, ignoreCase = true)
        }
    }

    @Test
    fun `Should recommend Irinotecan monotherapy in second line after first-line Oxaliplatin treatment`() {
        assertSpecificTreatmentNotRecommended(IRINOTECAN)
        assertThat(resultsForPatientWithHistory(listOf(CAPOX)))
            .anyMatch { it.treatment.name == IRINOTECAN }
    }

    @Test
    fun `Should require Oxaliplatin or Irinotecan for SOC exhaustion`() {
        val pastTreatmentNames = listOf(
            PEMBROLIZUMAB,
            "CAPECITABINE+BEVACIZUMAB",
            "CETUXIMAB",
            LONSURF
        )
        val patientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames)
        assertThat(resultsForPatient(patientRecord).map { it.treatment.name }.toSet()).contains(
            CAPOX,
            IRINOTECAN,
            FOLFOX
        )
        assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(patientRecord)).isFalse

        listOf(CAPOX, IRINOTECAN).forEach { treatment ->
            val updatedPatientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames + treatment)
            assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(updatedPatientRecord)).isTrue()
        }
    }

    @Test
    fun `Should require 5-FU or Capecitabine for SOC exhaustion`() {
        val pastTreatmentNames = listOf(
            PEMBROLIZUMAB,
            "OXALIPLATIN+BEVACIZUMAB",
            "CETUXIMAB",
            LONSURF
        )
        val patientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames)
        assertThat(resultsForPatient(patientRecord).map { it.treatment.name }.toSet()).contains(
            "CAPECITABINE", "FLUOROURACIL", FOLFOXIRI
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
                it.treatment.name.equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should not recommend the same chemotherapy after stop reason PD`() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTreatmentHistoryEntry(
                treatmentHistoryEntry(
                    treatmentSetWithName(treatmentName), stopReason = StopReason.PROGRESSIVE_DISEASE, startYear = HISTORICAL_YEAR
                )
            )
            assertThat(resultsForPatient(patientRecord)).noneMatch {
                it.treatment.name.equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should not recommend the same chemotherapy after best response PD`() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTreatmentHistoryEntry(
                treatmentHistoryEntry(
                    treatmentSetWithName(treatmentName), stopReason = StopReason.PROGRESSIVE_DISEASE, startYear = HISTORICAL_YEAR
                )
            )
            assertThat(resultsForPatient(patientRecord)).noneMatch {
                it.treatment.name.equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should not recommend the same chemotherapy after 12 cycles`() {
        CHEMO_TREATMENT_NAMES.forEach { treatmentName: String ->
            val patientRecord: PatientRecord = patientWithTreatmentHistoryEntry(
                treatmentHistoryEntry(treatmentSetWithName(treatmentName), numCycles = 12, startYear = HISTORICAL_YEAR)
            )
            assertThat(resultsForPatient(patientRecord)).noneMatch {
                it.treatment.name.equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria but with right sided tumor`() {
        val antiEgfrTreatments = setOf(CETUXIMAB, PANITUMUMAB)
        assertThat(resultsForPatientWithHistoryAndMolecular(listOf(CAPOX), MINIMAL_PATIENT_RECORD.molecular, "Ascending colon")
            .filter { (it.treatment as DrugTreatment).drugs.any { drug -> drug.name.uppercase() in antiEgfrTreatments } }).isEmpty()
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria who have already received anti-EGFR monotherapy`() {
        val firstLineChemotherapies = listOf(CAPOX)
        val antiEgfrTreatments = setOf(CETUXIMAB, PANITUMUMAB)
        antiEgfrTreatments.forEach { antiEgfrTreatment ->
            assertThat(resultsForPatientWithHistory(firstLineChemotherapies + antiEgfrTreatment)
                .filter { (it.treatment as DrugTreatment).drugs.any { drug -> drug.name.uppercase() in antiEgfrTreatments } }).isEmpty()
        }
    }

    @Test
    fun `Should not combine anti-EGFR therapy with capecitabine`() {
        val antiEGFRTherapies = antiEGFRTherapies()
        assertThat(antiEGFRTherapies).isNotEmpty
        assertThat(antiEGFRTherapies).noneMatch { it.drugs.any { drug -> drug.name.uppercase() == "CAPECITABINE" } }
    }

    @Test
    fun `Should not combine anti-EGFR therapy with FOLFOXIRI`() {
        val antiEGFRTherapies = antiEGFRTherapies()
        assertThat(antiEGFRTherapies).isNotEmpty
        listOf(CETUXIMAB, PANITUMUMAB).forEach { antiEGFRTreatment ->
            assertThat(antiEGFRTherapies).noneMatch { it.name.equals("FOLFOXIRI+$antiEGFRTreatment", ignoreCase = true) }
        }
    }

    private fun antiEGFRTherapies(): List<DrugTreatment> {
        return resultsForPatientWithHistory(listOf(CAPOX))
            .mapNotNull { it.treatment as? DrugTreatment }
            .filter {
                it.drugs.any { drug ->
                    drug.name.uppercase() == CETUXIMAB ||
                            drug.name.uppercase() == PANITUMUMAB
                }
            }
    }

    @Test
    fun `Should recommend Cetuximab+Encorafenib after first line for patient with BRAF V600E`() {
        val firstLineChemotherapies = listOf(CAPOX)
        val cetuximabAndEncorafenib = "CETUXIMAB+ENCORAFENIB"
        assertThat(resultsForPatientWithHistory(firstLineChemotherapies))
            .noneMatch { it.treatment.name.uppercase() == cetuximabAndEncorafenib }

        assertThat(resultsForPatientWithHistoryAndMolecular(emptyList(), MOLECULAR_RECORD_WITH_BRAF_V600E))
            .noneMatch { it.treatment.name.uppercase() == cetuximabAndEncorafenib }

        assertThat(resultsForPatientWithHistoryAndMolecular(firstLineChemotherapies, MOLECULAR_RECORD_WITH_BRAF_V600E))
            .anyMatch { it.treatment.name == cetuximabAndEncorafenib }
    }

    @Test
    fun `Should recommend optional Lonsurf after chemo and targeted therapy`() {
        val result = resultsForPatientWithHistoryAndMolecular(
            listOf(CAPOX, PANITUMUMAB), MINIMAL_PATIENT_RECORD.molecular
        ).find { treatmentCandidateMatchesName(it, LONSURF) }
        assertThat(result).isNotNull
        assertThat(result?.isOptional).isTrue
    }

    @Test
    fun `Should not recommend Lonsurf after Trifluridine`() {
        val results = resultsForPatientWithHistoryAndMolecular(
            listOf(CAPOX, "trifluridine", PANITUMUMAB),
            MINIMAL_PATIENT_RECORD.molecular
        )
        assertThat(results).noneMatch { treatmentCandidateMatchesName(it, LONSURF) }
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

    companion object {
        private val ACTIN_RESOURCE_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "actin-resources-private"
        ).joinToString(File.separator)

        private val TREATMENT_JSON_PATH = ACTIN_RESOURCE_PATH + File.separator + "treatment_db"

        private val TREATMENT_DATABASE = TreatmentDatabaseFactory.createFromPath(TREATMENT_JSON_PATH)

        private val RECOMMENDATION_ENGINE = RecommendationEngine.create(
            TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer"),
            AtcTree.createFromFile(listOf(ACTIN_RESOURCE_PATH, "atc_config", "atc_tree.tsv").joinToString(File.separator)),
            TreatmentCandidateDatabase(TREATMENT_DATABASE),
            ReferenceDateProviderTestFactory.createCurrentDateProvider()
        )

        private val CHEMO_TREATMENT_NAMES = listOf(
            "5-FU",
            "CAPECITABINE",
            "IRINOTECAN",
            "OXALIPLATIN",
            CAPOX,
            FOLFIRI,
            FOLFOXIRI,
            FOLFOX
        )

        private val MINIMAL_PATIENT_RECORD = TestDataFactory.createMinimalTestPatientRecord()
        private val MINIMAL_CRC_PATIENT_RECORD = MINIMAL_PATIENT_RECORD.copy(
            clinical = MINIMAL_PATIENT_RECORD.clinical.copy(
                tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID))
            )
        )

        private val MOLECULAR_RECORD_WITH_BRAF_V600E = TestMolecularFactory.createProperTestMolecularRecord()

        private val TYPICAL_TREATMENT_RESULTS: List<TreatmentCandidate> = resultsForPatientWithHistory(emptyList())

        private val HISTORICAL_YEAR = LocalDate.now().minusYears(3).year

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
            return patientRecordWithHistoryAndMolecular(pastTreatmentNames, MINIMAL_PATIENT_RECORD.molecular)
        }

        private fun patientRecordWithHistoryAndMolecular(
            pastTreatmentNames: List<String>, molecularRecord: MolecularRecord, tumorSubLocation: String? = null
        ): PatientRecord {
            val tumorDetails = TumorDetails(
                doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID),
                primaryTumorSubLocation = tumorSubLocation
            )
            val clinicalRecord = MINIMAL_PATIENT_RECORD.clinical.copy(
                tumor = tumorDetails,
                oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames)
            )
            return PatientRecordFactory.fromInputs(clinicalRecord, molecularRecord)
        }

        private fun assertSpecificTreatmentNotRecommended(name: String) {
            assertThat(TYPICAL_TREATMENT_RESULTS).noneMatch { treatmentCandidateMatchesName(it, name) }
        }

        private fun treatmentCandidateMatchesName(treatmentCandidate: TreatmentCandidate, name: String): Boolean {
            return treatmentCandidate.treatment.name.equals(name, ignoreCase = true) || treatmentCandidate.treatment.synonyms.any {
                it.equals(name, ignoreCase = true)
            }
        }

        private fun patientRecordWithTumorDoids(tumorDoid: String): PatientRecord {
            val tumorDetails = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID, tumorDoid))
            return MINIMAL_PATIENT_RECORD.copy(clinical = MINIMAL_PATIENT_RECORD.clinical.copy(tumor = tumorDetails))
        }

        private fun patientWithTreatmentHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): PatientRecord {
            return MINIMAL_CRC_PATIENT_RECORD.copy(
                clinical = MINIMAL_CRC_PATIENT_RECORD.clinical.copy(
                    oncologicalHistory = listOf(treatmentHistoryEntry)
                )
            )
        }

        private fun treatmentHistoryFromNames(names: List<String>): List<TreatmentHistoryEntry> {
            val treatmentDate = LocalDate.now().minusMonths(4)
            return names.map { treatmentName ->
                treatmentHistoryEntry(
                    treatments = treatmentSetWithName(treatmentName), startYear = treatmentDate.year, startMonth = treatmentDate.monthValue
                )
            }
        }

        private fun treatmentSetWithName(treatmentName: String) = setOf(TREATMENT_DATABASE.findTreatmentByName(treatmentName)!!)
    }
}