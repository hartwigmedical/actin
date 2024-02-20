package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Ignore
import org.junit.Test

class HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypesTest {
    @Test
    fun `Should fail if treatment history contains no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if treatment history contains no treatment in category`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    treatment(MATCHING_TREATMENT_NAME, true),
                    drugTreatment("test", DIFFERENT_CATEGORY)
                )
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should match only on treatment name and category if no types are provided`() {
        val function = HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
            treatment(MATCHING_TREATMENT_NAME, true), MATCHING_CATEGORY,
            emptySet()
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(HISTORY))
    }

    @Test
    @Ignore
    fun `Should fail if types provided but none match treatment history`() {
        val function = HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
            treatment(MATCHING_TREATMENT_NAME, true), MATCHING_CATEGORY, DIFFERENT_TYPES
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(HISTORY))
    }

    @Test
    fun `Should return undetermined if treatment is in different set from category and types`() {
        val history = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    listOf(
                        treatment(MATCHING_TREATMENT_NAME, true),
                        drugTreatment("other treatment", DIFFERENT_CATEGORY, MATCHING_TYPES)
                    )
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    listOf(
                        treatment("different treatment name", true),
                        drugTreatment("treatment", MATCHING_CATEGORY, MATCHING_TYPES)
                    )
                )
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(history)
        )
    }

    @Test
    @Ignore
    fun `Should pass if treatment occurred concurrently with category and single type`() {
        val function = HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
            treatment(MATCHING_TREATMENT_NAME, true), MATCHING_CATEGORY,
            setOf(DrugType.HER2_ANTIBODY)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(HISTORY))
    }

    @Test
    @Ignore
    fun `Should pass if treatment occurred concurrently with category and multiple types`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(HISTORY))
    }

    companion object {
        private const val MATCHING_TREATMENT_NAME = "treatment a"
        private val MATCHING_CATEGORY = TreatmentCategory.CHEMOTHERAPY
        private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
        private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
        private val DIFFERENT_TYPES = setOf(DrugType.ABL_INHIBITOR)

        private val HISTORY = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    listOf(
                        treatment(MATCHING_TREATMENT_NAME, true),
                        drugTreatment("same drug", MATCHING_CATEGORY, MATCHING_TYPES)
                    )
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    listOf(
                        treatment("some different treatment instance", true),
                        drugTreatment("same drug", MATCHING_CATEGORY, MATCHING_TYPES)
                    )
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    listOf(
                        treatment("completely different treatment", true),
                        drugTreatment("different drug", TreatmentCategory.TRIAL, DIFFERENT_TYPES)
                    )
                )
            )
        )

        private val FUNCTION =
            HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
                treatment(MATCHING_TREATMENT_NAME, true), MATCHING_CATEGORY, MATCHING_TYPES
            )
    }
}