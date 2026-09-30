package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTest {

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(withTreatmentHistory(emptyList())),
            "No HER2 antibody targeted therapy treatment with PD"
        )
    }

    @Test
    fun `Should fail for wrong category with PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "No HER2 antibody targeted therapy treatment with PD"
        )
    }

    @Test
    fun `Should select right fail messages when wrong category with PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        val result1 = function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        val result2 = function(minWeeks = 10).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        val result3 = function(minCycles = 10).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))

        assertThat(result1.failMessagesStrings()).containsExactly("No HER2 antibody targeted therapy treatment with PD")
        assertThat(result2.failMessagesStrings()).containsExactly("No HER2 antibody targeted therapy treatment with PD for at least 10 weeks")
        assertThat(result3.failMessagesStrings()).containsExactly("No HER2 antibody targeted therapy treatment with PD and at least 10 cycles")
    }

    @Test
    fun `Should fail for right category and type but no PD`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.TOXICITY, bestResponse = TreatmentResponse.MIXED)
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "No PD after targeted therapy"
        )
    }

    @Test
    fun `Should select right fail messages when right category and type but not PD`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.TOXICITY, bestResponse = TreatmentResponse.MIXED)
        val result1 = function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        val result2 = function(minWeeks = 10).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        val result3 = function(minCycles = 10).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))

        assertThat(result1.failMessagesStrings()).containsExactly("No PD after targeted therapy")
        assertThat(result2.failMessagesStrings()).containsExactly("No PD after targeted therapy for at least 10 weeks")
        assertThat(result3.failMessagesStrings()).containsExactly("No PD after targeted therapy and at least 10 cycles")
    }

    @Test
    fun `Should return undetermined for right category and missing type with PD`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "Undetermined history of HER2 antibody targeted therapy treatment based on provided treatments"
        )
    }

    @Test
    fun `Should return undetermined for right category type and missing stop reason`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment in provided treatments but uncertain if there has been PD"
        )
    }

    @Test
    fun `Should pass for right category type with no stop reason but subsequent treatment line within 26 weeks`() {
        val matchingEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopYear = 2020, stopMonth = 6)
        val subsequentEntry = treatmentHistoryEntry(setOf(drugTreatment("other", TreatmentCategory.CHEMOTHERAPY)), startYear = 2020, startMonth = 9)
        assertEvaluation(
            EvaluationResult.PASS,
            function().evaluate(withTreatmentHistory(listOf(matchingEntry, subsequentEntry))),
            "HER2 antibody targeted therapy treatment with PD in provided treatments"
        )
    }

    @Test
    fun `Should pass for right category type and stop reason PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(
            EvaluationResult.PASS,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD in provided treatments"
        )
    }

    @Test
    fun `Should pass for matching treatment when PD is indicated in best response`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        assertEvaluation(
            EvaluationResult.PASS,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD in provided treatments"
        )
    }

    @Test
    fun `Should return undetermined with trial treatment entry in history`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function().evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "Undetermined history of HER2 antibody targeted therapy treatment based on provided treatments"
        )
    }

    @Test
    fun `Should ignore trial matches when looking for unlikely trial categories`() {
        val function = HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
            TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
            null, null
        )
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "No allogenic transplantation treatment with PD"
        )
    }

    @Test
    fun `Should return undetermined for right category type and stop reason PD when cycles are required and missing`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function(minCycles = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD but unknown nr of cycles in provided treatments"
        )
    }

    @Test
    fun `Should warn for right category type and stop reason PD when cycles are required and insufficient`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE, numCycles = 4
        )
        assertEvaluation(
            EvaluationResult.WARN,
            function(minCycles = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD but less than 5 cycles in provided treatments"
        )
    }

    @Test
    fun `Should pass for right category type and stop reason PD when cycles are required and sufficient`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE, numCycles = 5
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function(minCycles = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD and at least 5 cycles in provided treatments"
        )
    }

    @Test
    fun `Should return undetermined for right category type and stop reason PD when min weeks required and unknown`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function(minWeeks = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD but unknown nr of weeks in provided treatments"
        )
    }

    @Test
    fun `Should fail for right category type and stop reason PD when min weeks required and insufficient`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 5
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function(minWeeks = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD but less than 5 weeks in provided treatments"
        )
    }

    @Test
    fun `Should pass for right category type and stop reason PD when min weeks required and sufficient`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 6
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function(minWeeks = 5).evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment with PD for at least 5 weeks in provided treatments"
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val MATCHING_TREATMENT_SET = setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET))

        private fun function(minCycles: Int? = null, minWeeks: Int? = null): HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks {
            return HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                MATCHING_CATEGORY, MATCHING_TYPE_SET, minCycles, minWeeks
            )
        }
    }
}