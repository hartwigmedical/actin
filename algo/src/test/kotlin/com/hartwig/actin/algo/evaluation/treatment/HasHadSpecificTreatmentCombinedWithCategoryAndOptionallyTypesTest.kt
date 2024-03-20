package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypesTest {

    @Test
    fun `Should fail if treatment history contains no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if history contains treatment with right category and type but not combined with treatment with required name`() {
        val treatmentHistory =
            withTreatmentHistory(
                listOf(
                    treatmentHistoryEntry(setOf(drugTreatment("wrong name", MATCHING_CATEGORY, emptySet()))),
                    treatmentHistoryEntry(setOf(drugTreatment("other drug", MATCHING_CATEGORY, MATCHING_TYPES)))
                )
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(treatmentHistory))
    }

    @Test
    fun `Should fail if treatment history contains no treatment with required category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(MATCHING_TREATMENT_NAME, DIFFERENT_CATEGORY),
                drugTreatment("wrong name", DIFFERENT_CATEGORY)
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if the named one is correct but there is no other from the required category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(MATCHING_TREATMENT_NAME, MATCHING_CATEGORY),
                drugTreatment("test", DIFFERENT_CATEGORY)
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass on treatment name and category if function requires no types`() {
        val function =
            HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
                drugTreatment(MATCHING_TREATMENT_NAME, DIFFERENT_CATEGORY), MATCHING_CATEGORY, emptySet()
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(HISTORY))
    }

    @Test
    fun `Should fail if types required but none match treatment history`() {
        val history = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    listOf(
                        drugTreatment(MATCHING_TREATMENT_NAME, MATCHING_CATEGORY),
                        drugTreatment("some drug", MATCHING_CATEGORY, DIFFERENT_TYPES)
                    )
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(history))
    }

    @Test
    fun `Should return undetermined if treatment is in different set from category and types in patient history`() {
        val history = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    listOf(
                        drugTreatment(MATCHING_TREATMENT_NAME, MATCHING_CATEGORY),
                        drugTreatment("other treatment", DIFFERENT_CATEGORY, DIFFERENT_TYPES)
                    )
                ),
                treatmentHistoryEntry(
                    listOf(
                        drugTreatment("different treatment name", MATCHING_CATEGORY),
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
    fun `Should pass if single type is provided and patient record contains treatment with multiple including the provided one`() {
        val function = HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
            treatment(MATCHING_TREATMENT_NAME, true), MATCHING_CATEGORY,
            setOf(DrugType.HER2_ANTIBODY)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(HISTORY))
    }

    @Test
    fun `Should pass if multiple types provided and patient record contains treatment with multiple types`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(HISTORY))
    }

    companion object {
        private const val MATCHING_TREATMENT_NAME = "treatment a"
        private val MATCHING_CATEGORY = TreatmentCategory.CHEMOTHERAPY
        private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
        private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
        private val DIFFERENT_TYPES = setOf(DrugType.ABL_INHIBITOR)

        private val HISTORY = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    listOf(
                        drugTreatment(MATCHING_TREATMENT_NAME, MATCHING_CATEGORY),
                        drugTreatment("same drug", MATCHING_CATEGORY, MATCHING_TYPES)
                    )
                ),
                treatmentHistoryEntry(
                    listOf(
                        drugTreatment("some different treatment instance", MATCHING_CATEGORY),
                        drugTreatment("same drug", MATCHING_CATEGORY, MATCHING_TYPES)
                    )
                ),
                treatmentHistoryEntry(
                    listOf(
                        drugTreatment("completely different treatment", MATCHING_CATEGORY),
                        drugTreatment("different drug", TreatmentCategory.IMMUNOTHERAPY, DIFFERENT_TYPES)
                    )
                )
            )
        )

        private val FUNCTION =
            HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
                drugTreatment(MATCHING_TREATMENT_NAME, DIFFERENT_CATEGORY), MATCHING_CATEGORY, MATCHING_TYPES
            )
    }
}