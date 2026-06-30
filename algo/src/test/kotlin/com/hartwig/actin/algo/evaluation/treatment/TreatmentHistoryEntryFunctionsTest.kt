package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.evaluateIfDrugHadPDResponse
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private val TARGET_DRUG_SET = setOf(Drug("Osimertinib", emptySet(), setOf(DrugType.EGFR_INHIBITOR), TreatmentCategory.TARGETED_THERAPY))
private val TARGET_DRUG_TREATMENT = drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR))
private val WRONG_DRUG_TREATMENT = drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
private val DRUG_TREATMENT_WITH_TARGET_CATEGORY = drugTreatment("Target therapy trial drug", TreatmentCategory.TARGETED_THERAPY, emptySet())
private val TRIAL_DRUG_TREATMENT_NO_CATEGORY = drugTreatment("Some trial drug", TreatmentCategory.TARGETED_THERAPY, emptySet())

class TreatmentHistoryEntryFunctionsTest {

    private val predicate: (Treatment) -> Boolean = { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }

    @Test
    fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when treatment history is empty`() {
        assertThat(evaluateIfDrugHadPDResponse(emptyList(), TARGET_DRUG_SET)).isEqualTo(
            TreatmentHistoryEntryFunctions.TreatmentHistoryEvaluation(
                matchingDrugsWithPD = emptySet(),
                matchingDrugs = emptySet(),
                matchesWithUnclearPD = false,
                possibleTrialMatch = false,
                matchesWithToxicity = false
            )
        )
    }

    @Test
    fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when target drug set is empty`() {
        val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TARGET_DRUG_TREATMENT))
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), emptySet())).isEqualTo(
            TreatmentHistoryEntryFunctions.TreatmentHistoryEvaluation(
                matchingDrugsWithPD = emptySet(),
                matchingDrugs = emptySet(),
                matchesWithUnclearPD = false,
                possibleTrialMatch = false,
                matchesWithToxicity = false
            )
        )
    }

    @Test
    fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when target drugs not in history`() {
        val treatmentHistory = treatmentHistoryEntry(treatments = setOf(WRONG_DRUG_TREATMENT))
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), emptySet())).isEqualTo(
            TreatmentHistoryEntryFunctions.TreatmentHistoryEvaluation(
                matchingDrugsWithPD = emptySet(),
                matchingDrugs = emptySet(),
                matchesWithUnclearPD = false,
                possibleTrialMatch = false,
                matchesWithToxicity = false
            )
        )
    }

    @Test
    fun `Should correctly return all matching drugs with PD as matchingDrugsWithPD`() {
        val treatmentHistory = treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
            stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchingDrugsWithPD).containsAll(
            TARGET_DRUG_TREATMENT.drugs
        )
    }

    @Test
    fun `Should return empty set for matchingDrugsWithPD if no PD occurred with any matching drug`() {
        val treatmentHistory = treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
            stopReason = StopReason.TOXICITY
        )
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchingDrugsWithPD).isEmpty()
    }

    @Test
    fun `Should return true for matchesWithUnclearPD if all matching drugs have stop reason null`() {
        val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT), stopReason = null)
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchesWithUnclearPD).isTrue()
    }

    @Test
    fun `Should return true for possible trial match when target drug is trial and correct category`() {
        val treatmentHistory = treatmentHistoryEntry(treatments = setOf(DRUG_TREATMENT_WITH_TARGET_CATEGORY), isTrial = true)
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).possibleTrialMatch).isTrue
    }

    @Test
    fun `Should return true for possible trial match when target drug is trial and no category configured`() {
        val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TRIAL_DRUG_TREATMENT_NO_CATEGORY), isTrial = true)
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).possibleTrialMatch).isTrue
    }

    @Test
    fun `Should return true for matchesWithToxicity when target drug has stop reason toxicity`() {
        val treatmentHistory = treatmentHistoryEntry(treatments = setOf(TARGET_DRUG_TREATMENT), stopReason = StopReason.TOXICITY)
        assertThat(evaluateIfDrugHadPDResponse(listOf(treatmentHistory), TARGET_DRUG_SET).matchesWithToxicity).isTrue
    }

    @Test
    fun `Should return unmodified entry for matching single-stage treatment`() {
        val entry = treatmentHistoryEntry(setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)))
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate)).isEqualTo(entry)
    }

    @Test
    fun `Should return single-stage entry with aggregated treatments and cycles for matching multi-stage treatment`() {
        val maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY), cycles = 3)
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = entry.treatments + maintenanceTreatment.treatment,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(cycles = 5, maintenanceTreatment = null)
                )
            )
    }

    @Test
    fun `Should override stop date in treatment details when later treatment stages do not match`() {
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT)),
            numCycles = 2
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(entry.copy(treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(maintenanceTreatment = null)))
    }

    @Test
    fun `Should override stop date to start of non-matching maintenance stage`() {
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT), startYear = 2021, startMonth = 4
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        cycles = 2,
                        maintenanceTreatment = null,
                        stopYear = maintenanceTreatment.startYear,
                        stopMonth = maintenanceTreatment.startMonth
                    )
                )
            )
    }

    @Test
    fun `Should aggregate matching additional stage treatment with base treatment`() {
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2021, startMonth = 4
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = entry.treatments + maintenanceTreatment.treatment,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        cycles = 2,
                        maintenanceTreatment = null
                    )
                )
            )
    }

    @Test
    fun `Should return entry representing matching stages when base treatment does not match`() {
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2021, startMonth = 4
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.TARGETED_THERAPY)),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            startYear = 2019,
            startMonth = 8,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = setOf(maintenanceTreatment.treatment),
                    startYear = maintenanceTreatment.startYear,
                    startMonth = maintenanceTreatment.startMonth,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        maintenanceTreatment = null,
                        cycles = null
                    )
                )
            )
    }

    @Test
    fun `Should return null when no stages of entry match`() {
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT)
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.TARGETED_THERAPY)),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate)).isNull()
    }

    @Test
    fun `Should display maintenance treatments when present`() {
        val maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY))
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2
        )
        assertThat(TreatmentHistoryEntryFunctions.fullTreatmentDisplay(entry)).isEqualTo(
            "Test treatment continued with Maintenance treatment maintenance"
        )
    }

    @Test
    fun `Should display base treatment when no maintenance treatments present`() {
        val entry = treatmentHistoryEntry(setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)), numCycles = 2)
        assertThat(TreatmentHistoryEntryFunctions.fullTreatmentDisplay(entry)).isEqualTo("Test treatment")
    }

    @Test
    fun `Should return number of weeks between the start and stop date of the treatment`() {
        val entry = treatmentHistoryEntry(startYear = 2024, startMonth = 3, stopYear = 2024, stopMonth = 8)
        assertThat(TreatmentHistoryEntryFunctions.weeksBetweenDates(entry)).isEqualTo(17)
    }

    @Test
    fun `Should return max number of weeks between the start and stop date of the treatment`() {
        val entry = TreatmentHistoryEntry(treatments = emptySet(), startYear = 2024, startMonth = 3, treatmentHistoryDetails = TreatmentHistoryDetails(maxStopYear = 2024, maxStopMonth = 8))
        assertThat(TreatmentHistoryEntryFunctions.maxWeeksBetweenDates(entry)).isEqualTo(17)
    }
}