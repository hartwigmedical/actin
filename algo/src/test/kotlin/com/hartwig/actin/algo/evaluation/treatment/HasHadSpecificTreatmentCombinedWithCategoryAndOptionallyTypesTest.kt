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
    fun `Should fail if treatment history contains no treatment with given name`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(MATCHING_TREATMENT_NAME + "asdf", MATCHING_CATEGORY, emptySet())
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if treatment history contains no treatment in category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(MATCHING_TREATMENT_NAME, DIFFERENT_CATEGORY),
                drugTreatment("test", DIFFERENT_CATEGORY)
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if the only treatment in the given category in the patient history is the named one`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(MATCHING_TREATMENT_NAME, MATCHING_CATEGORY),
                drugTreatment("test", DIFFERENT_CATEGORY)
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should match only on treatment name and category if function contains no types`() {
        val function =
            HasHadSpecificTreatmentCombinedWithCategoryAndOptionallyTypes(
                drugTreatment(MATCHING_TREATMENT_NAME, DIFFERENT_CATEGORY), MATCHING_CATEGORY, emptySet()
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(HISTORY))
    }

    @Test
    fun `Should fail if types provided to function but none match treatment history`() {
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