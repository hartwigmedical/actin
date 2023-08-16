package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.drugTherapy
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithCategoryButNotOfTypesTest {
    @Test
    fun shouldFailForNoTreatments() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForWrongTreatmentCategory() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldFailForTreatmentWithCorrectCategoryAndIgnoreType() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", MATCHING_CATEGORY, IGNORE_TYPE_SET)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForTrialTreatment() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldIgnoreTrialMatchesAndFailWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadTreatmentWithCategoryButNotOfTypes(TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC))
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTherapy("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldPassForCorrectTreatmentCategoryWithOtherType() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTherapy("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR)))
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val IGNORE_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val FUNCTION = HasHadTreatmentWithCategoryButNotOfTypes(MATCHING_CATEGORY, IGNORE_TYPE_SET)
    }
}