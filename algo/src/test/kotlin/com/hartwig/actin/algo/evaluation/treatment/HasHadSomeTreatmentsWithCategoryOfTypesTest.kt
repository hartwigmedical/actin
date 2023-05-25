package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadSomeTreatmentsWithCategoryOfTypesTest {
    @Test
    fun canEvaluate() {
        val category = TreatmentCategory.TARGETED_THERAPY
        val function = HasHadSomeTreatmentsWithCategoryOfTypes(category, listOf("Anti-EGFR", "Anti-EGFR"), 2)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category with wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some other type").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add another correct treatment category with right type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("Some anti-EGFR treatment").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add another correct treatment category but with no type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add another correct treatment category with right type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("Another anti-EGFR").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        val function = HasHadSomeTreatmentsWithCategoryOfTypes(TreatmentCategory.TARGETED_THERAPY, emptyList(), 1)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.TRIAL)
                        .build()
                )
            )
        )
    }
}