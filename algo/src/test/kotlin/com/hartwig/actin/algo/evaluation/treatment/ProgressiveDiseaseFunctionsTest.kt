package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.evaluateTreatmentHistory
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val START_YEAR = 1999
private const val START_MONTH = 1
private const val STOP_YEAR = 1999
private const val STOP_MONTH_SUFFICIENT_DURATION = 9
private const val STOP_MONTH_INSUFFICIENT_DURATION = 4
private val TARGET_DRUG_SET = setOf(
    Drug("Osimertinib", setOf(DrugType.EGFR_INHIBITOR), TreatmentCategory.TARGETED_THERAPY)
)
private val TARGET_DRUG_TREATMENT =
    TreatmentTestFactory.drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR))
private val WRONG_DRUG_TREATMENT =
    TreatmentTestFactory.drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
private val DRUG_TREATMENT_WITH_TARGET_CATEGORY =
    TreatmentTestFactory.drugTreatment("Target therapy trial drug", TreatmentCategory.TARGETED_THERAPY, emptySet())
private val TRIAL_DRUG_TREATMENT_NO_CATEGORY =
    TreatmentTestFactory.drugTreatment("Some trial drug", TreatmentCategory.TARGETED_THERAPY, emptySet())

class ProgressiveDiseaseFunctionsTest {

    // Testing of fun treatmentResultedInPD
    @Test
    fun `Should return true when stop reason is null and best response is PD and duration null`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(null, TreatmentResponse.PROGRESSIVE_DISEASE))).isTrue()
    }

    @Test
    fun `Should return true when stop reason is PD and best response is null and duration null`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, null))).isTrue()
    }

    @Test
    fun `Should return true when stop reason is null and duration was sufficient`() {
        assertThat(
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    null,
                    null,
                    STOP_MONTH_SUFFICIENT_DURATION
                )
            )
        ).isTrue()
    }

    @Test
    fun `Should return null when stop reason is null and duration was insufficient`() {
        assertThat(
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    null,
                    null,
                    STOP_MONTH_INSUFFICIENT_DURATION
                )
            )
        ).isNull()
    }

    @Test
    fun `Should return null when stop reason is null and best response is not PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(null, TreatmentResponse.MIXED))).isNull()
    }

    @Test
    fun `Should return false when stop reason is not PD and best response is null`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, null))).isFalse()
    }

    @Test
    fun `Should return true when stop reason is PD and best response is not PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.MIXED))).isTrue()
    }

    @Test
    fun `Should return true when stop reason is not PD and best response is PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.PROGRESSIVE_DISEASE))).isTrue()
    }

    @Test
    fun `Should return false when stop reason is not PD`() {
        assertThat(treatmentResultedInPD(treatmentHistoryEntry(StopReason.TOXICITY, TreatmentResponse.MIXED))).isFalse()
    }

    @Test
    fun `Should return false when stop reason is not PD also if treatment duration was sufficient`() {
        assertThat(
            treatmentResultedInPD(
                treatmentHistoryEntryWithDates(
                    StopReason.TOXICITY,
                    TreatmentResponse.MIXED,
                    STOP_MONTH_SUFFICIENT_DURATION
                )
            )
        ).isFalse()
    }

    // Testing of fun evaluateTreatmentHistory
    @Test
    fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when treatment history is empty`() {
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistory(emptyList()), TARGET_DRUG_SET)
        ).isEqualTo(
            ProgressiveDiseaseFunctions.TreatmentHistoryEvaluation(
                matchingDrugsWithPD = emptySet(),
                matchingDrugs = emptySet(),
                matchesWithUnclearPD = false,
                possibleTrialMatch = false,
                matchesWithToxicity = false
            )
        )
    }

    @Test
    fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when target drug set is empty`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), emptySet())
        ).isEqualTo(
            ProgressiveDiseaseFunctions.TreatmentHistoryEvaluation(
                matchingDrugsWithPD = emptySet(),
                matchingDrugs = emptySet(),
                matchesWithUnclearPD = false,
                possibleTrialMatch = false,
                matchesWithToxicity = false
            )
        )
    }

    @Test
    fun `Should return TreatmentHistoryEvaluation object with empty sets and false Booleans when target drugs not in history`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(WRONG_DRUG_TREATMENT),
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), emptySet())
        ).isEqualTo(
            ProgressiveDiseaseFunctions.TreatmentHistoryEvaluation(
                matchingDrugsWithPD = emptySet(),
                matchingDrugs = emptySet(),
                matchesWithUnclearPD = false,
                possibleTrialMatch = false,
                matchesWithToxicity = false
            )
        )
    }

    @Test
    fun `Should correctly return all matching drugs with PD as matchingDrugsWithPD`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
            stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), TARGET_DRUG_SET).matchingDrugsWithPD
        ).containsAll(TARGET_DRUG_TREATMENT.drugs)
    }

    @Test
    fun `Should return empty set for matchingDrugsWithPD if no PD occurred with any matching drug`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
            stopReason = StopReason.TOXICITY
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), TARGET_DRUG_SET).matchingDrugsWithPD
        ).isEmpty()
    }

    @Test
    fun `Should return true for matchesWithUnclearPD if all matching drugs have stop reason null`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT, WRONG_DRUG_TREATMENT),
            stopReason = null
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), TARGET_DRUG_SET).matchesWithUnclearPD
        ).isTrue()
    }

    @Test
    fun `Should return true for possible trial match when target drug is trial and correct category`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(DRUG_TREATMENT_WITH_TARGET_CATEGORY),
            isTrial = true
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), TARGET_DRUG_SET).possibleTrialMatch
        ).isTrue
    }

    @Test
    fun `Should return true for possible trial match when target drug is trial and no category configured`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TRIAL_DRUG_TREATMENT_NO_CATEGORY),
            isTrial = true
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), TARGET_DRUG_SET).possibleTrialMatch
        ).isTrue
    }

    @Test
    fun `Should return true for matchesWithToxicity when target drug has stop reason toxicity`(){
        val treatmentHistory = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
            stopReason = StopReason.TOXICITY
        )
        assertThat(
            evaluateTreatmentHistory(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistory), TARGET_DRUG_SET).matchesWithToxicity
        ).isTrue
    }

    private fun treatmentHistoryEntry(stopReason: StopReason?, bestResponse: TreatmentResponse?): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("test treatment", true)),
            stopReason = stopReason,
            bestResponse = bestResponse
        )
    }

    private fun treatmentHistoryEntryWithDates(
        stopReason: StopReason?,
        bestResponse: TreatmentResponse?,
        stopMonth: Int?
    ): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("test treatment", true)),
            stopReason = stopReason,
            bestResponse = bestResponse,
            startYear = START_YEAR,
            startMonth = START_MONTH,
            stopYear = STOP_YEAR,
            stopMonth = stopMonth
        )
    }
}