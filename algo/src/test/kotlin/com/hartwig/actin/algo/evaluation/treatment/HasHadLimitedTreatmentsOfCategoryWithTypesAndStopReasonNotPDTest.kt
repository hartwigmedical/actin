package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPDTest {

    @Test
    fun `Should fail for empty treatments`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should fail for wrong category`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.TOXICITY
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail for right category and type but with PD`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                MATCHING_TREATMENT_SET,
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.MIXED
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for right category and missing type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)),
                stopReason = StopReason.TOXICITY
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for right category type and missing stop reason`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
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
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail for matching treatment when PD is indicated in best response`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined with trial treatment entry with matching category in history`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
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
    fun `Should return undetermined for right category type and stop reason other than PD when weeks are missing`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.TOXICITY, startYear = null)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail for right category type and stop reason other than PD when treatment duration more than max weeks`() {
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
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val MATCHING_TREATMENT_SET = setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET))
        private val function = HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(MATCHING_CATEGORY, MATCHING_TYPE_SET, 6)
    }
}