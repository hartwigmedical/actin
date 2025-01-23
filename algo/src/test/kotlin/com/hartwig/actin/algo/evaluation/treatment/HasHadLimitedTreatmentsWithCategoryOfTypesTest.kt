package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadLimitedTreatmentsWithCategoryOfTypesTest {

    @Test
    fun `Should pass in case patient had no treatments and treatment is optional`() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail in case patient had no treatments and treatment is required`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass when treatments with correct category and type within limit whether treatment is required or optional`() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(MATCHING_TREATMENT))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(MATCHING_TREATMENT))))
    }

    @Test
    fun `Should pass when treatments with correct category and one of the types within limit whether treatment is required or optional`() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(MATCHING_TREATMENT_ONE_TYPE))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(MATCHING_TREATMENT_ONE_TYPE))))
    }

    @Test
    fun `Should pass if patient has had only treatment of wrong category and treatment is optional`() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_WRONG_CATEGORY)))
        )
    }

    @Test
    fun `Should fail if patient has had only treatment of wrong category and treatment is required`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_WRONG_CATEGORY)))
        )
    }

    @Test
    fun `Should pass when treatments with correct category with wrong type within limit and treatment is optional`() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_WRONG_TYPE)))
        )
    }

    @Test
    fun `Should fail when treatments with correct category with wrong type within limit and treatment is required`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_WRONG_TYPE)))
        )
    }

    @Test
    fun `Should pass when treatments with correct category but missing type possibly within limit and treatment is optional`() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_UNKNOWN_TYPE)))
        )
    }

    @Test
    fun `Should be undetermined when treatments with correct category but missing type possibly within limit and treatment is required`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(NON_MATCHING_TREATMENT_UNKNOWN_TYPE)))
        )
    }

    @Test
    fun `Should be undetermined when treatments with correct category but missing type possibly exceeding limit whether treatment is required or optional`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(
                withTreatmentHistory(
                    listOf(
                        NON_MATCHING_TREATMENT_UNKNOWN_TYPE,
                        NON_MATCHING_TREATMENT_UNKNOWN_TYPE
                    )
                )
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_REQUIRED.evaluate(
                withTreatmentHistory(
                    listOf(
                        NON_MATCHING_TREATMENT_UNKNOWN_TYPE,
                        NON_MATCHING_TREATMENT_UNKNOWN_TYPE
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when treatments with correct category exceed limit whether treatment is required or optional`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(MATCHING_TREATMENT, MATCHING_TREATMENT)))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(MATCHING_TREATMENT, MATCHING_TREATMENT)))
        )
    }

    @Test
    fun `Should pass if there is a potentially matching trial option within the limit and treatment is optional`() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(TRIAL_TREATMENT_UNKNOWN_CATEGORY)))
        )
    }

    @Test
    fun `Should be undetermined if there is a potentially matching trial option within the limit and treatment is required`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(TRIAL_TREATMENT_UNKNOWN_CATEGORY)))
        )
    }

    @Test
    fun `Should be undetermined when possibly matching trial options could exceed limit whether treatment is required or optional`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY
                    )
                )
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_REQUIRED.evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY
                    )
                )
            )
        )
    }

    @Test
    fun `Should ignore trial matches and pass when looking for unlikely trial categories and treatment is optional`() {
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadLimitedTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 1, false).evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY
                    )
                )
            )
        )
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories and treatment is required`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadLimitedTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 1, true).evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_UNKNOWN_CATEGORY
                    )
                )
            )
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
        private val MATCHING_TREATMENT = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, types = MATCHING_TYPE_SET)))
        private val MATCHING_TREATMENT_ONE_TYPE = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, types = setOf(MATCHING_TYPE_SET.first()))))
        private val NON_MATCHING_TREATMENT_WRONG_CATEGORY =
            treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        private val NON_MATCHING_TREATMENT_WRONG_TYPE =
            treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, types = setOf(DrugType.ANTI_TISSUE_FACTOR))))
        private val NON_MATCHING_TREATMENT_UNKNOWN_TYPE = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        private val TRIAL_TREATMENT_UNKNOWN_CATEGORY = treatmentHistoryEntry(setOf(treatment("trial", true, emptySet())), isTrial = true)

        private val FUNCTION_TREATMENT_OPTIONAL = HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, 1, false)
        private val FUNCTION_TREATMENT_REQUIRED = HasHadLimitedTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, 1, true)
    }
}