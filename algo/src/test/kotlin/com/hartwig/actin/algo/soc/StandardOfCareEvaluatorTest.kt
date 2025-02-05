package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createMinimalTestOrangeRecord
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import java.time.LocalDate

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

private val RECENT_DATE = LocalDate.now().minusMonths(4)
private val HISTORICAL_DATE = LocalDate.now().minusYears(3)

class StandardOfCareEvaluatorTest {

    private val treatmentDatabase = mockk<TreatmentDatabase> {
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

    private val treatmentCandidateDatabase = TreatmentCandidateDatabase(treatmentDatabase)
    private val alwaysAvailableTreatments = CrcDecisionTree.commonChemotherapies.map {
        treatmentDatabase.findTreatmentByName(it)!!
    }
    private val commonFirstLineTherapies = CrcDecisionTree.commonChemotherapies.map {
        treatmentCandidateDatabase.treatmentCandidateWithBevacizumab(it).treatment
    }

    private val socEvaluator = StandardOfCareEvaluatorFactory(
        RuleMappingResourcesTestFactory.create(
            doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer"),
            treatmentDatabase = treatmentDatabase
        )
    ).create()

    private val minimalMolecularRecord = createMinimalTestOrangeRecord().copy(
        characteristics = createMinimalTestOrangeRecord().characteristics.copy(
            isMicrosatelliteUnstable = false,
            isHomologousRecombinationDeficient = false
        )
    )
    private val molecularRecordWithBrafV600e = TestMolecularFactory.createProperTestOrangeRecord()
    private val msiMolecularRecord = minimalMolecularRecord.copy(
        characteristics = minimalMolecularRecord.characteristics.copy(isMicrosatelliteUnstable = true)
    )
    private val molecularRecordWithOtherBrafMutation = minimalMolecularRecord.copy(
        drivers = minimalMolecularRecord.drivers.copy(
            variants = listOf(
                TestVariantFactory.createMinimal().copy(
                    canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = "p.D594A"),
                    isReportable = true
                )
            )
        )
    )
    private val minimalPatientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord()
        .copy(molecularHistory = MolecularHistory(listOf(minimalMolecularRecord)))
    private val minimalCrcPatientRecord = minimalPatientRecord.copy(
        tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID))
    )

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
                socEvaluator.patientHasExhaustedStandardOfCare(
                    patientRecordWithTreatmentHistory(pastTreatmentNames + chemotherapy)
                )
            ).isFalse
        }

        assertThat(
            socEvaluator.patientHasExhaustedStandardOfCare(
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
        assertThat(
            resultsForPatientWithHistoryAndMolecular(listOf(CAPOX), minimalMolecularRecord, "Ascending colon")
                .filter { (it.treatment as DrugTreatment).drugs.any { drug -> drug.name.uppercase() in antiEgfrTreatments } }).isEmpty()
    }

    @Test
    fun `Should not recommend anti-EGFR therapy for patients matching molecular criteria who have already received anti-EGFR monotherapy`() {
        val firstLineChemotherapies = listOf(CAPOX)
        val antiEgfrTreatments = setOf(CETUXIMAB, PANITUMUMAB)
        antiEgfrTreatments.forEach { antiEgfrTreatment ->
            assertThat(
                resultsForPatientWithHistory(firstLineChemotherapies + antiEgfrTreatment)
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
        val patientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), molecularRecordWithOtherBrafMutation, "rectum")
        val firstLineEgfrTherapies = listOf(
            FOLFOX_CETUXIMAB, FOLFOX_PANITUMUMAB, FOLFIRI_CETUXIMAB, FOLFIRI_PANITUMUMAB, IRINOTECAN_CETUXIMAB, IRINOTECAN_PANITUMUMAB
        ).map(treatmentDatabase::findTreatmentByName)

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + commonFirstLineTherapies + firstLineEgfrTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patients with RAS wildtype and BRAF V600E wildtype and left-sided tumors in second line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY"), molecularRecordWithOtherBrafMutation, "rectum"
        )
        val expectedAdditionalTherapies = listOf(
            FOLFIRI_CETUXIMAB, FOLFIRI_PANITUMUMAB, IRINOTECAN_CETUXIMAB, IRINOTECAN_PANITUMUMAB, CETUXIMAB, PANITUMUMAB, IRINOTECAN
        ).map(treatmentDatabase::findTreatmentByName)

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + expectedAdditionalTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patients with RAS wildtype and BRAF V600E wildtype and left-sided tumors in third line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), molecularRecordWithOtherBrafMutation, "rectum"
        )
        val expectedAdditionalTherapies = listOf(
            CETUXIMAB,
            PANITUMUMAB,
            IRINOTECAN,
            TRIFLURIDINE_TIPIRACIL,
            TRIFLURIDINE_TIPIRACIL_BEVACIZUMAB
        ).map(treatmentDatabase::findTreatmentByName)

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + expectedAdditionalTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patient with BRAF V600E in first line`() {
        val firstLinePatientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), molecularRecordWithBrafV600e)
        assertThat(firstLinePatientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + commonFirstLineTherapies)
    }

    @Test
    fun `Should recommend expected treatments for patient with BRAF V600E in second line`() {
        val secondLinePatientResults = resultsForPatientWithHistoryAndMolecular(listOf("CHEMOTHERAPY"), molecularRecordWithBrafV600e)
        val expectedAdditionalSecondLineCandidates = listOf(ENCORAFENIB_CETUXIMAB, IRINOTECAN).map(treatmentDatabase::findTreatmentByName)
        assertThat(secondLinePatientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + expectedAdditionalSecondLineCandidates)
    }

    @Test
    fun `Should recommend expected treatments for patient with BRAF V600E in third line`() {
        val thirdLinePatientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), molecularRecordWithBrafV600e
        )
        val expectedAdditionalCandidates = listOf(
            ENCORAFENIB_CETUXIMAB,
            IRINOTECAN,
            TRIFLURIDINE_TIPIRACIL,
            TRIFLURIDINE_TIPIRACIL_BEVACIZUMAB
        ).map(treatmentDatabase::findTreatmentByName)
        assertThat(thirdLinePatientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + expectedAdditionalCandidates)
    }

    @Test
    fun `Should recommend expected treatments for BRAF V600E wildtype patients who don't qualify for EGFR therapy in first line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), minimalMolecularRecord, "ascending colon")

        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + commonFirstLineTherapies)
    }

    @Test
    fun `Should recommend expected treatments for BRAF V600E wildtype patients who don't qualify for EGFR therapy in second line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY"), minimalMolecularRecord, "ascending colon"
        )
        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + treatmentDatabase.findTreatmentByName(IRINOTECAN))
    }

    @Test
    fun `Should recommend expected treatments for BRAF V600E wildtype patients who don't qualify for EGFR therapy in third line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), minimalMolecularRecord, "ascending colon"
        )
        val expectedAdditionalCandidates =
            listOf(IRINOTECAN, TRIFLURIDINE_TIPIRACIL, TRIFLURIDINE_TIPIRACIL_BEVACIZUMAB).map(treatmentDatabase::findTreatmentByName)
        assertThat(patientResults.map(TreatmentCandidate::treatment))
            .containsExactlyInAnyOrderElementsOf(alwaysAvailableTreatments + expectedAdditionalCandidates)
    }

    @Test
    fun `Should recommend Pembrolizumab for MSI patients in first line`() {
        val patientResults = resultsForPatientWithHistoryAndMolecular(emptyList(), msiMolecularRecord)
        assertThat(patientResults.map(TreatmentCandidate::treatment)).contains(treatmentDatabase.findTreatmentByName(PEMBROLIZUMAB))
    }

    @Test
    fun `Should recommend Pembrolizumab and Nivolumab for MSI patients after first line`() {
        val expectedAdditionalTreatments = listOf(PEMBROLIZUMAB, NIVOLUMAB).map(treatmentDatabase::findTreatmentByName)

        val secondLinePatientResults = resultsForPatientWithHistoryAndMolecular(listOf("CHEMOTHERAPY"), msiMolecularRecord)
        assertThat(secondLinePatientResults.map(TreatmentCandidate::treatment)).containsAll(expectedAdditionalTreatments)

        val thirdLinePatientResults = resultsForPatientWithHistoryAndMolecular(
            listOf("CHEMOTHERAPY", "TARGETED_THERAPY"), msiMolecularRecord
        )
        assertThat(thirdLinePatientResults.map(TreatmentCandidate::treatment)).containsAll(expectedAdditionalTreatments)
    }

    @Test
    fun `Should recommend Entrectinib and Larotrectinib for patients with NTRK fusion`() {
        val ntrkFusionTreatments = listOf(ENTRECTINIB, LAROTRECTINIB).map(treatmentDatabase::findTreatmentByName)
        val patientWithNtrkFusion = MolecularTestFactory.withFusion(
            TestFusionFactory.createMinimal().copy(
                geneStart = "NTRK1",
                geneEnd = "NTRK1",
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
            )
        ).copy(tumor = minimalCrcPatientRecord.tumor)

        val pastTreatmentNames = listOf(FOLFOX, IRINOTECAN, FLUOROURACIL, CAPECITABINE, CETUXIMAB, "TARGETED_THERAPY")
        val patientWithNtrkFusionAndSocExhaustion = patientWithNtrkFusion.copy(
            oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames),
            molecularHistory = MolecularHistory(
                listOf(
                    patientWithNtrkFusion.molecularHistory.latestOrangeMolecularRecord()!!
                        .copy(characteristics = minimalMolecularRecord.characteristics)
                )
            )
        )
        assertThat(socEvaluator.patientHasExhaustedStandardOfCare(patientWithNtrkFusionAndSocExhaustion)).isTrue
        assertThat(resultsForPatient(patientWithNtrkFusionAndSocExhaustion).map(TreatmentCandidate::treatment))
            .containsAll(ntrkFusionTreatments)
    }

    @Test
    fun `Should report SOC exhaustion if all required treatments in recent history`() {
        val pastTreatmentNames = listOf(FOLFIRI, CAPOX, CETUXIMAB)
        assertThat(socEvaluator.patientHasExhaustedStandardOfCare(patientRecordWithTreatmentHistory(pastTreatmentNames))).isTrue
    }

    @Test
    fun `Should report SOC exhaustion if all required treatments in history even if years have passed`() {
        val pastTreatmentNames = listOf(FOLFIRI, CAPOX, CETUXIMAB)
        val patient = minimalCrcPatientRecord.copy(oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames, HISTORICAL_DATE))
        assertThat(socEvaluator.patientHasExhaustedStandardOfCare(patient)).isTrue
    }

    @Test
    fun `Should throw exception if patient does not have colorectal cancer`() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { resultsForPatient(minimalPatientRecord) }
    }

    @Test
    fun `Should throw exception if patient has excluded DOID`() {
        listOf("5777", "169", "1800").forEach { doid: String ->
            assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
                resultsForPatient(patientRecordWithTumorDoids(doid))
            }
        }
    }

    private fun drug(name: String): Drug {
        val category = if (name == "TARGETED_THERAPY" || name.endsWith("B")) {
            TreatmentCategory.TARGETED_THERAPY
        } else {
            TreatmentCategory.CHEMOTHERAPY
        }
        return Drug(name, emptySet(), category)
    }

    private fun resultsForPatient(patientRecord: PatientRecord): List<TreatmentCandidate> {
        return socEvaluator.standardOfCareEvaluatedTreatments(patientRecord).potentiallyEligibleTreatments()
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
        return patientRecordWithHistoryAndMolecular(pastTreatmentNames, minimalMolecularRecord)
    }

    private fun patientRecordWithHistoryAndMolecular(
        pastTreatmentNames: List<String>, molecularRecord: MolecularRecord, tumorSubLocation: String? = null
    ): PatientRecord {
        val tumorDetails = TumorDetails(
            doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID),
            primaryTumorSubLocation = tumorSubLocation
        )
        val patientRecord = minimalPatientRecord.copy(
            tumor = tumorDetails,
            oncologicalHistory = treatmentHistoryFromNames(pastTreatmentNames),
            molecularHistory = MolecularHistory(listOf(molecularRecord))
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
        return minimalPatientRecord.copy(tumor = tumorDetails)
    }

    private fun patientWithTreatmentHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): PatientRecord {
        return minimalCrcPatientRecord.copy(
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

    private fun treatmentSetWithName(treatmentName: String) = setOf(treatmentDatabase.findTreatmentByName(treatmentName)!!)
}