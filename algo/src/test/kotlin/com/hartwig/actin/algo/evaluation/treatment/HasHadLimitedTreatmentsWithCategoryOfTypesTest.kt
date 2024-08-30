package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadLimitedTreatmentsWithCategoryOfTypesTest {

    @Test
    fun shouldPassForNoTreatments() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassForWrongTreatmentCategory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldPassForCorrectTreatmentCategoryWithWrongType() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR))))
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldFailWhenTreatmentsWithCorrectCategoryAndTypeExceedLimit() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenTreatmentsWithCorrectCategoryAndNoTypeExceedLimit() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenTrialTreatmentsExceedLimit() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun shouldIgnoreTrialMatchesAndPassWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadLimitedTreatmentsWithCategoryOfTypes(TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC), 1)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val FUNCTION = HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, 1)
    }
}