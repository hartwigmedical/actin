package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadLimitedTreatmentsWithCategoryOfTypesTest {
    @Test
    fun canEvaluate() {
        val category = TreatmentCategory.TARGETED_THERAPY
        val types: List<String> = listOf("anti-BRAF", "anti-KRAS")
        val function = HasHadLimitedTreatmentsWithCategoryOfTypes(category, types, 1)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category with wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-EGFR").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category with correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-BRAF").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category without a type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category with another correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-KRAS").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        val category = TreatmentCategory.TARGETED_THERAPY
        val function = HasHadLimitedTreatmentsWithCategoryOfTypes(category, emptyList(), 1)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one trial
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add another trial
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}