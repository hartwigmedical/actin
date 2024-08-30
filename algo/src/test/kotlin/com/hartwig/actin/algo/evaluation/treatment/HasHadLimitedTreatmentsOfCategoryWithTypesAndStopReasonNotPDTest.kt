package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPDTest {

    @Test
    fun `Should fail for empty treatments`() {
        evaluateFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(emptyList()))
    }

    @Test
    fun `Should fail for wrong category`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.TOXICITY
        )
        evaluateFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should fail for right category and type but with PD`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                MATCHING_TREATMENT_SET,
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.MIXED
            )
        evaluateFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should return undetermined for right category and missing type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)),
                stopReason = StopReason.TOXICITY
            )
        evaluateFunctions(EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should return undetermined for right category type and missing stop reason`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET)
        evaluateFunctions(EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should pass for right category type with stop reason other than PD with any amount of weeks if weeks not requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2024,
            stopMonth = 4
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should pass for right category type with stop reason other than PD with unknown amount of weeks if weeks not requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = null,
            startMonth = null,
            stopYear = null,
            stopMonth = null
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should pass for right category type within requested amount of weeks and with stop reason other than PD`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 4
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail for matching treatment when PD is indicated in best response`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        evaluateFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should return undetermined with trial treatment entry with matching category in history`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        evaluateFunctions(EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should ignore trial matches when looking for unlikely trial categories`() {
        val function = HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
            TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
            null, null
        )
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("test", true)), isTrial = true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for right category type and stop reason other than PD when weeks are missing and weeks requested`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.TOXICITY, startYear = null)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail for right category type and stop reason other than PD when treatment duration more than max weeks and weeks requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 6
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            functionWithWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        EvaluationAssert.assertEvaluation(expected, functionWithWeeks.evaluate(record))
        EvaluationAssert.assertEvaluation(expected, functionWithoutWeeks.evaluate(record))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val MATCHING_TREATMENT_SET = setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET))
        private val functionWithWeeks = HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(MATCHING_CATEGORY, MATCHING_TYPE_SET, 6)
        private val functionWithoutWeeks =
            HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(MATCHING_CATEGORY, MATCHING_TYPE_SET, null)
    }
}