package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import java.time.LocalDate

class RecommendationEngineTest {

    @Test
    fun `Should not recommend Oxaliplatin monotherapy`() {
        assertSpecificTreatmentNotRecommended(OXALIPLATIN)
    }

    @Test
    fun `Should not recommend Bevacizumab monotherapy`() {
        assertSpecificTreatmentNotRecommended(BEVACIZUMAB)
    }

    @Test
    fun `Should not recommend Oxaliplatin+Bevacizumab`() {
        // Oxaliplatin should always be given in combination with 5-FU or Capecitabine
        assertSpecificTreatmentNotRecommended("$OXALIPLATIN+$BEVACIZUMAB")
    }

    @Test
    fun `Should not recommend Irinotecan+Bevacizumab`() {
        // This is never given in the Netherlands
        assertSpecificTreatmentNotRecommended("$IRINOTECAN+$BEVACIZUMAB")
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
    fun `Should not recommend FOLFOX after FOLFOXIRI`() {
        assertThat(resultsForPatientWithHistory(listOf(FOLFOXIRI))).noneMatch {
            it.treatment.name.equals(FOLFOX, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend FOLFIRI after CAPIRI`() {
        assertThat(resultsForPatientWithHistory(listOf(CAPIRI))).noneMatch {
            it.treatment.name.equals(FOLFIRI, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend CAPIRI after FOLFIRI`() {
        assertThat(resultsForPatientWithHistory(listOf(FOLFIRI))).noneMatch {
            it.treatment.name.equals(CAPIRI, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend FOLFIRI after FOLFIRI + bevacizumab`() {
        assertThat(resultsForPatientWithHistory(listOf("FOLFIRI+BEVACIZUMAB"))).noneMatch {
            it.treatment.name.equals(FOLFIRI, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend 5-FU after Capecitabine`() {
        assertThat(resultsForPatientWithHistory(listOf(CAPECITABINE))).noneMatch {
            it.treatment.name.equals(FLUOROURACIL, ignoreCase = true)
        }
    }

    @Test
    fun `Should not recommend Capecitabine after 5-FU`() {
        assertThat(resultsForPatientWithHistory(listOf(FLUOROURACIL))).noneMatch {
            it.treatment.name.equals(CAPECITABINE, ignoreCase = true)
        }
    }

    @Test
    fun `Should recommend Irinotecan monotherapy in second line after first-line Oxaliplatin treatment`() {
        assertSpecificTreatmentNotRecommended(IRINOTECAN)
        assertThat(resultsForPatientWithHistory(listOf(CAPOX)))
            .anyMatch { it.treatment.name == IRINOTECAN }
    }

    @Test
    fun `Should require Oxaliplatin, Irinotecan, 5-FU, and Capecitabine for SOC exhaustion`() {
        val chemotherapies = listOf(CAPOX, FOLFOX, IRINOTECAN, FLUOROURACIL, CAPECITABINE)
        val pastTreatmentNames = listOf(
            PEMBROLIZUMAB,
            CETUXIMAB
        )
        val patientRecord = patientRecordWithTreatmentHistory(pastTreatmentNames)
        assertThat(resultsForPatient(patientRecord).map { it.treatment.name }.toSet()).containsAll(chemotherapies)

        chemotherapies.forEach { chemotherapy ->
            assertThat(
                RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(
                    patientRecordWithTreatmentHistory(pastTreatmentNames + chemotherapy)
                )
            ).isFalse
        }

        assertThat(
            RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(
                patientRecordWithTreatmentHistory(pastTreatmentNames + chemotherapies)
            )
        ).isTrue
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
                    treatmentSetWithName(treatmentName), stopReason = StopReason.PROGRESSIVE_DISEASE, startYear = HISTORICAL_DATE.year
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
                    treatmentSetWithName(treatmentName), stopReason = StopReason.PROGRESSIVE_DISEASE, startYear = HISTORICAL_DATE.year
                )
            )
            assertThat(resultsForPatient(patientRecord)).noneMatch {
                it.treatment.name.equals(treatmentName, ignoreCase = true)
            }
        }
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria but with right sided tumor`() {
        val antiEgfrTreatments = setOf(CETUXIMAB, PANITUMUMAB)
        assertThat(resultsForPatientWithHistoryAndMolecular(listOf(CAPOX), MINIMAL_MOLECULAR_RECORD, "Ascending colon")
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
        assertThat(antiEGFRTherapies).noneMatch { it.drugs.any { drug -> drug.name.uppercase() == CAPECITABINE } }
    }

    @Test
    fun `Should not combine anti-EGFR therapy with FOLFOXIRI`() {
        val antiEGFRTherapies = antiEGFRTherapies()
        assertThat(antiEGFRTherapies).isNotEmpty
        listOf(CETUXIMAB, PANITUMUMAB).forEach { antiEGFRTreatment ->
            assertThat(antiEGFRTherapies).noneMatch { it.name.equals("$FOLFOXIRI+$antiEGFRTreatment", ignoreCase = true) }
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
    fun `Should recommend expected treatments for patients with RAS wildtype and BRAF V600E wildtype and left-sided tumors in first line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), MOLECULAR_RECORD_WITH_OTHER_BRAF_MUTATION, "rectum")
        val firstLineEgfrTherapies = listOf(
            FOLFOX_CETUXIMAB, FOLFOX_PANITUMUMAB, FOLFIRI_CETUXIMAB, FOLFIRI_PANITUMUMAB, IRINOTECAN_CETUXIMAB, IRINOTECAN_PANITUMUMAB
        ).map(TREATMENT_DATABASE::findTreatmentByName)

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + COMMON_FIRST_LINE_CHEMOTHERAPIES + firstLineEgfrTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patients with RAS wildtype and BRAF V600E wildtype and left-sided tumors in second line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY"), MOLECULAR_RECORD_WITH_OTHER_BRAF_MUTATION, "rectum"
        )
        val expectedAdditionalTherapies = listOf(
            FOLFIRI_CETUXIMAB, FOLFIRI_PANITUMUMAB, IRINOTECAN_CETUXIMAB, IRINOTECAN_PANITUMUMAB, CETUXIMAB, PANITUMUMAB, IRINOTECAN
        ).map(TREATMENT_DATABASE::findTreatmentByName)

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + expectedAdditionalTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patients with RAS wildtype and BRAF V600E wildtype and left-sided tumors in third line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), MOLECULAR_RECORD_WITH_OTHER_BRAF_MUTATION, "rectum"
        )
        val expectedAdditionalTherapies =
            listOf(CETUXIMAB, PANITUMUMAB, IRINOTECAN, TRIFLURIDINE_TIPIRACIL).map(TREATMENT_DATABASE::findTreatmentByName)

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + expectedAdditionalTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patient with BRAF V600E in first line`() {
        val firstLinePatientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), MOLECULAR_RECORD_WITH_BRAF_V600E)
        assertThat(firstLinePatientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + COMMON_FIRST_LINE_CHEMOTHERAPIES)
    }

    @Test
    fun `Should recommend expected treatments for patient with BRAF V600E in second line`() {
        val secondLinePatientResults = resultsForPatientWithHistoryAndMolecular(listOf("CHEMOTHERAPY"), MOLECULAR_RECORD_WITH_BRAF_V600E)
        val expectedAdditionalSecondLineCandidates = listOf(ENCORAFENIB_CETUXIMAB, IRINOTECAN).map(TREATMENT_DATABASE::findTreatmentByName)
        assertThat(secondLinePatientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + expectedAdditionalSecondLineCandidates)
    }

    @Test
    fun `Should recommend expected treatments for patient with BRAF V600E in third line`() {
        val thirdLinePatientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), MOLECULAR_RECORD_WITH_BRAF_V600E
        )
        val expectedAdditionalCandidates =
            listOf(ENCORAFENIB_CETUXIMAB, IRINOTECAN, TRIFLURIDINE_TIPIRACIL).map(TREATMENT_DATABASE::findTreatmentByName)
        assertThat(thirdLinePatientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + expectedAdditionalCandidates)
    }

    @Test
    fun `Should recommend expected treatments for BRAF V600E wildtype patients who don't qualify for EGFR therapy in first line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), MINIMAL_MOLECULAR_RECORD, "ascending colon")

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + COMMON_FIRST_LINE_CHEMOTHERAPIES)
    }

    @Test
    fun `Should recommend expected treatments for BRAF V600E wildtype patients who don't qualify for EGFR therapy in second line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY"), MINIMAL_MOLECULAR_RECORD, "ascending colon"
        )
        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + TREATMENT_DATABASE.findTreatmentByName(IRINOTECAN))
    }

    @Test
    fun `Should recommend expected treatments for BRAF V600E wildtype patients who don't qualify for EGFR therapy in third line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), MINIMAL_MOLECULAR_RECORD, "ascending colon"
        )
        val expectedAdditionalCandidates = listOf(IRINOTECAN, TRIFLURIDINE_TIPIRACIL).map(TREATMENT_DATABASE::findTreatmentByName)
        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(ALWAYS_AVAILABLE_TREATMENTS + expectedAdditionalCandidates)
    }

    @Test
    fun `Should recommend Pembrolizumab for MSI patients in first line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), MSI_MOLECULAR_RECORD)
        assertThat(patientResults.map(TreatmentCandidate::treatment)).contains(TREATMENT_DATABASE.findTreatmentByName(PEMBROLIZUMAB))
    }

    @Test
    fun `Should recommend Pembrolizumab and Nivolumab for MSI patients after first line`() {
        val expectedAdditionalTreatments = listOf(PEMBROLIZUMAB, NIVOLUMAB).map(TREATMENT_DATABASE::findTreatmentByName)

        val secondLinePatientResults = resultsForPatientWithHistoryAndMolecular(listOf("CHEMOTHERAPY"), MSI_MOLECULAR_RECORD)
        assertThat(secondLinePatientResults.map(TreatmentCandidate::treatment)).containsAll(expectedAdditionalTreatments)

        val thirdLinePatientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), MSI_MOLECULAR_RECORD
        )
        assertThat(thirdLinePatientResults.map(TreatmentCandidate::treatment)).containsAll(expectedAdditionalTreatments)
    }

    @Test
    fun `Should recommend Entrectinib and Larotrectinib for patients with NTRK fusion`() {
        val ntrkFusionTreatments = listOf(ENTRECTINIB, LAROTRECTINIB).map(TREATMENT_DATABASE::findTreatmentByName)
        val patientWithNtrkFusion = MolecularTestFactory.withFusion(
            TestFusionFactory.createMinimal().copy(
                geneStart = "NTRK1",
                geneEnd = "NTRK1",
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
            )
        ).copy(tumor = MINIMAL_CRC_PATIENT_RECORD.tumor)

        val pastTreatmentNames = listOf(FOLFOX, IRINOTECAN, FLUOROURACIL, CAPECITABINE, CETUXIMAB, "TARGETED_THERAPY")
        val patientWithNtrkFusionAndSocExhaustion = patientWithNtrkFusion.copy(
            oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames)
        )
        assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(patientWithNtrkFusionAndSocExhaustion)).isTrue
        assertThat(resultsForPatient(patientWithNtrkFusionAndSocExhaustion).map(TreatmentCandidate::treatment))
            .containsAll(ntrkFusionTreatments)
    }

    @Test
    fun `Should report SOC exhaustion if all required treatments in recent history`() {
        val pastTreatmentNames = listOf(FOLFIRI, CAPOX, CETUXIMAB)
        assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(patientRecordWithTreatmentHistory(pastTreatmentNames))).isTrue
    }

    @Test
    fun `Should report SOC exhaustion if all required treatments in history even if years have passed`() {
        val pastTreatmentNames = listOf(FOLFIRI, CAPOX, CETUXIMAB)
        val patient = MINIMAL_CRC_PATIENT_RECORD.copy(oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames, HISTORICAL_DATE))
        assertThat(RECOMMENDATION_ENGINE.patientHasExhaustedStandardOfCare(patient)).isTrue
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
        private val TREATMENT_DATABASE = mockk<TreatmentDatabase> {
            every { findTreatmentByName(any()) } answers {

                val treatmentName = firstArg<String>()
                val drugs = treatmentName.split("+").flatMap { subTreatmentName ->
                    when (subTreatmentName) {
                        CAPIRI -> listOf("CAPECITABINE", "IRINOTECAN")
                        CAPOX -> listOf("CAPECITABINE", "OXALIPLATIN")
                        FOLFIRI -> listOf("FLUOROURACIL", "IRINOTECAN")
                        FOLFOX -> listOf("FLUOROURACIL", "OXALIPLATIN")
                        FOLFOXIRI -> listOf("FLUOROURACIL", "OXALIPLATIN", "IRINOTECAN")
                        TRIFLURIDINE_TIPIRACIL -> listOf("TRIFLURIDINE", "TIPRACIL")
                        else -> listOf(subTreatmentName)
                    }
                }
                DrugTreatment(treatmentName, drugs.map(::drug).toSet())
            }
            every { findDrugByName(any()) } answers { drug(firstArg()) }
        }

        private val TREATMENT_CANDIDATE_DATABASE = TreatmentCandidateDatabase(TREATMENT_DATABASE)
        private val ALWAYS_AVAILABLE_TREATMENTS = CrcDecisionTree.commonChemotherapies.map {
            TREATMENT_DATABASE.findTreatmentByName(it)!!
        }
        private val COMMON_FIRST_LINE_CHEMOTHERAPIES = CrcDecisionTree.commonChemotherapies.map {
            TREATMENT_CANDIDATE_DATABASE.treatmentCandidateWithBevacizumab(it).treatment
        }

        private val RECOMMENDATION_ENGINE = RecommendationEngineFactory(
            RuleMappingResourcesTestFactory.create(
                doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer"),
                treatmentDatabase = TREATMENT_DATABASE
            )
        ).create()

        private val CHEMO_TREATMENT_NAMES = listOf(
            "5-FU",
            CAPECITABINE,
            IRINOTECAN,
            OXALIPLATIN,
            CAPOX,
            FOLFIRI,
            FOLFOXIRI,
            FOLFOX
        )

        private val MINIMAL_PATIENT_RECORD = TestPatientFactory.createMinimalTestWGSPatientRecord()
        private val MINIMAL_CRC_PATIENT_RECORD = MINIMAL_PATIENT_RECORD.copy(
            tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID))
        )

        private val MINIMAL_MOLECULAR_RECORD = TestMolecularFactory.createMinimalTestMolecularRecord()
        private val MOLECULAR_RECORD_WITH_BRAF_V600E = TestMolecularFactory.createProperTestMolecularRecord()
        private val MSI_MOLECULAR_RECORD = MINIMAL_MOLECULAR_RECORD.copy(
            characteristics = MINIMAL_MOLECULAR_RECORD.characteristics.copy(isMicrosatelliteUnstable = true)
        )
        private val MOLECULAR_RECORD_WITH_OTHER_BRAF_MUTATION = MINIMAL_MOLECULAR_RECORD.copy(
            drivers = MINIMAL_MOLECULAR_RECORD.drivers.copy(
                variants = setOf(
                    TestVariantFactory.createMinimal().copy(
                        canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = "p.D594A"),
                        isReportable = true
                    )
                )
            )
        )

        private val RECENT_DATE = LocalDate.now().minusMonths(4)
        private val HISTORICAL_DATE = LocalDate.now().minusYears(3)

        private fun drug(name: String): Drug {
            val category = if (name == "TARGETED_THERAPY" || name.endsWith("B")) {
                TreatmentCategory.TARGETED_THERAPY
            } else {
                TreatmentCategory.CHEMOTHERAPY
            }
            return Drug(name, emptySet(), category)
        }

        private fun resultsForPatient(patientRecord: PatientRecord): List<TreatmentCandidate> {
            return RECOMMENDATION_ENGINE.standardOfCareEvaluatedTreatments(patientRecord)
                .filter(EvaluatedTreatment::eligible)
                .map(EvaluatedTreatment::treatmentCandidate)
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
            return patientRecordWithHistoryAndMolecular(pastTreatmentNames, MINIMAL_MOLECULAR_RECORD)
        }

        private fun patientRecordWithHistoryAndMolecular(
            pastTreatmentNames: List<String>, molecularRecord: MolecularRecord, tumorSubLocation: String? = null
        ): PatientRecord {
            val tumorDetails = TumorDetails(
                doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID),
                primaryTumorSubLocation = tumorSubLocation
            )
            val patientRecord = MINIMAL_PATIENT_RECORD.copy(
                tumor = tumorDetails,
                oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames),
                molecularHistory = MolecularHistory.fromInputs(listOf(molecularRecord), emptyList())
            )
            return patientRecord
        }

        private fun assertSpecificTreatmentNotRecommended(name: String) {
            assertThat(resultsForPatientWithHistory(emptyList())).noneMatch { treatmentCandidateMatchesName(it, name) }
        }

        private fun treatmentCandidateMatchesName(treatmentCandidate: TreatmentCandidate, name: String): Boolean {
            return treatmentCandidate.treatment.name.equals(name, ignoreCase = true) || treatmentCandidate.treatment.synonyms.any {
                it.equals(name, ignoreCase = true)
            }
        }

        private fun patientRecordWithTumorDoids(tumorDoid: String): PatientRecord {
            val tumorDetails = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID, tumorDoid))
            return MINIMAL_PATIENT_RECORD.copy(tumor = tumorDetails)
        }

        private fun patientWithTreatmentHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): PatientRecord {
            return MINIMAL_CRC_PATIENT_RECORD.copy(
                oncologicalHistory = listOf(treatmentHistoryEntry)
            )
        }

        private fun treatmentHistoryFromNames(names: List<String>, treatmentDate: LocalDate = RECENT_DATE): List<TreatmentHistoryEntry> {
            return names.map { treatmentName ->
                treatmentHistoryEntry(
                    treatments = treatmentSetWithName(treatmentName), startYear = treatmentDate.year, startMonth = treatmentDate.monthValue
                )
            }
        }

        private fun treatmentSetWithName(treatmentName: String) = setOf(TREATMENT_DATABASE.findTreatmentByName(treatmentName)!!)
    }
}