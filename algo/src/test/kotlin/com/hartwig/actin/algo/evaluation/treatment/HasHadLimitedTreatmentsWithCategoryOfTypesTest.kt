package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test

private const val MAX_TREATMENT_LINES = 1
private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
private val MATCHING_TREATMENT_WITH_TYPES =
    treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, types = MATCHING_TYPE_SET)))
private val MATCHING_TREATMENT_WITH_ONE_TYPE =
    treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, types = setOf(MATCHING_TYPE_SET.first()))))
private val NON_MATCHING_TREATMENT_CATEGORY_AND_TYPES =
    treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY, types = setOf(DrugType.ANTI_TISSUE_FACTOR))))
private val NON_MATCHING_TREATMENT_ONLY_WRONG_TYPE =
    treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, types = setOf(DrugType.ANTI_TISSUE_FACTOR))))
private val NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
private val TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES =
    treatmentHistoryEntry(setOf(treatment("trial", true, emptySet())), isTrial = true)
private val RARE_CATEGORY = TreatmentCategory.TRANSPLANTATION

class HasHadLimitedTreatmentsWithCategoryOfTypesTest {
    private val functionTreatmentOptional = HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, null, MAX_TREATMENT_LINES, false)
    private val functionTreatmentOptionalWithTypes =
        HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, MAX_TREATMENT_LINES, false)
    private val functionTreatmentRequired = HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, null, MAX_TREATMENT_LINES, true)
    private val functionTreatmentRequiredWithTypes =
        HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, MAX_TREATMENT_LINES, true)

    @Test
    fun `Should pass in case patient had no treatments and treatment is optional`() {
        evaluateOptionalFunctions(EvaluationResult.PASS, emptyList())
    }

    @Test
    fun `Should fail in case patient had no treatments and treatment is required`() {
        evaluateRequiredFunctions(EvaluationResult.FAIL, emptyList())
    }

    @Test
    fun `Should pass when treatments with correct category within limit whether treatment is optional or required`() {
        evaluateOptionalFunctions(EvaluationResult.PASS, listOf(MATCHING_TREATMENT_WITH_TYPES))
        evaluateRequiredFunctions(EvaluationResult.PASS, listOf(MATCHING_TREATMENT_WITH_TYPES))
    }

    @Test
    fun `Should pass when treatments with correct category and one of the types within limit whether treatment is optional or required`() {
        evaluateOptionalFunctions(EvaluationResult.PASS, listOf(MATCHING_TREATMENT_WITH_ONE_TYPE))
        evaluateRequiredFunctions(EvaluationResult.PASS, listOf(MATCHING_TREATMENT_WITH_ONE_TYPE))
    }

    @Test
    fun `Should pass if patient has had only treatment of wrong category and treatment is optional`() {
        evaluateOptionalFunctions(EvaluationResult.PASS, listOf(NON_MATCHING_TREATMENT_CATEGORY_AND_TYPES))
    }

    @Test
    fun `Should fail if patient has had only treatment of wrong category and treatment is required`() {
        evaluateRequiredFunctions(EvaluationResult.FAIL, listOf(NON_MATCHING_TREATMENT_CATEGORY_AND_TYPES))
    }

    @Test
    fun `Should pass when treatments with correct category with wrong type within limit and treatment is optional and types required`() {
        assertEvaluation(
            EvaluationResult.PASS,
            functionTreatmentOptionalWithTypes.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_ONLY_WRONG_TYPE)))
        )
    }

    @Test
    fun `Should fail when treatments with correct category with wrong type within limit and treatment is required and types required`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionTreatmentRequiredWithTypes.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_ONLY_WRONG_TYPE)))
        )
    }

    @Test
    fun `Should pass when treatments with correct category but missing type possibly within limit and treatment is optional and types required`() {
        assertEvaluation(
            EvaluationResult.PASS,
            functionTreatmentOptionalWithTypes.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE)))
        )
    }

    @Test
    fun `Should be undetermined when treatments with correct category but missing type possibly within limit and treatment is required and types required`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionTreatmentRequiredWithTypes.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE)))
        )
    }

    @Test
    fun `Should be undetermined when treatments with correct category but missing type possibly exceeding limit whether treatment is optional or required and types required`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionTreatmentOptionalWithTypes.evaluate(
                withTreatmentHistory(
                    listOf(NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE, NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionTreatmentRequiredWithTypes.evaluate(
                withTreatmentHistory(
                    listOf(NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE, NON_MATCHING_TREATMENT_ONLY_UNKNOWN_TYPE)
                )
            )
        )
    }

    @Test
    fun `Should fail when treatments with correct category exceed limit whether treatment is optional or required`() {
        evaluateOptionalFunctions(EvaluationResult.FAIL, listOf(MATCHING_TREATMENT_WITH_TYPES, MATCHING_TREATMENT_WITH_TYPES))
        evaluateRequiredFunctions(EvaluationResult.FAIL, listOf(MATCHING_TREATMENT_WITH_TYPES, MATCHING_TREATMENT_WITH_TYPES))
    }

    @Test
    fun `Should pass if there is a potentially matching trial option within the limit and treatment is optional`() {
        evaluateOptionalFunctions(EvaluationResult.PASS, listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES))
    }

    @Test
    fun `Should be undetermined if there is a potentially matching trial option within the limit and treatment is required`() {
        evaluateRequiredFunctions(EvaluationResult.UNDETERMINED, listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES))
    }

    @Test
    fun `Should be undetermined when possibly matching trial options could exceed limit whether treatment is optional or required`() {
        evaluateOptionalFunctions(
            EvaluationResult.UNDETERMINED,
            listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES)
        )
        evaluateRequiredFunctions(
            EvaluationResult.UNDETERMINED,
            listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES)
        )
    }

    @Test
    fun `Should ignore trial matches and pass when looking for unlikely trial categories and treatment is optional`() {
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadLimitedTreatmentsWithCategoryOfTypes(RARE_CATEGORY, null, 1, false).evaluate(
                withTreatmentHistory(
                    listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadLimitedTreatmentsWithCategoryOfTypes(RARE_CATEGORY, MATCHING_TYPE_SET, 1, false).evaluate(
                withTreatmentHistory(
                    listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES)
                )
            )
        )
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories and treatment is required`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadLimitedTreatmentsWithCategoryOfTypes(RARE_CATEGORY, null, 1, true).evaluate(
                withTreatmentHistory(
                    listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadLimitedTreatmentsWithCategoryOfTypes(RARE_CATEGORY, MATCHING_TYPE_SET, 1, true).evaluate(
                withTreatmentHistory(
                    listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY_AND_TYPES)
                )
            )
        )
    }

    private fun evaluateOptionalFunctions(result: EvaluationResult, treatmentList: List<TreatmentHistoryEntry>) {
        assertEvaluation(result, functionTreatmentOptional.evaluate(withTreatmentHistory(treatmentList)))
        assertEvaluation(result, functionTreatmentOptionalWithTypes.evaluate(withTreatmentHistory(treatmentList)))
    }

    private fun evaluateRequiredFunctions(result: EvaluationResult, treatmentList: List<TreatmentHistoryEntry>) {
        assertEvaluation(result, functionTreatmentRequired.evaluate(withTreatmentHistory(treatmentList)))
        assertEvaluation(result, functionTreatmentRequiredWithTypes.evaluate(withTreatmentHistory(treatmentList)))
    }
}