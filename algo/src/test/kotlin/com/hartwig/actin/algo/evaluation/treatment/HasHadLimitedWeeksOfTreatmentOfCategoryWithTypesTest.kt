package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import org.junit.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
private val MATCHING_TREATMENT_SET = setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET))

class HasHadLimitedWeeksOfTreatmentOfCategoryWithTypesTest {
    private val function = HasHadLimitedWeeksOfTreatmentOfCategoryWithTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, 6)

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong category`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.TOXICITY
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for right category and missing type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)),
                stopReason = StopReason.TOXICITY
            )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for right category type within requested amount of weeks`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 4
        )

        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined with trial treatment entry with matching category in history`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should ignore trial matches when looking for unlikely trial categories`() {
        val function = HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
            TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
            null, null
        )
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("test", true)), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for right category type when weeks are missing and weeks requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, startYear = null)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail for right category type when treatment duration more than max weeks and weeks requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 6
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }
}