package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithCategoryButNotOfTypesTest {
    @Test
    fun canEvaluate() {
        val category = TreatmentCategory.TARGETED_THERAPY
        val ignoreTypes: List<String> = listOf("type1", "type2")
        val function = HasHadTreatmentWithCategoryButNotOfTypes(category, ignoreTypes)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category but with ignore type 1
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType(ignoreTypes[0]).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category but with ignore type 2
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType(ignoreTypes[1]).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add trial
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment category and correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("pass me").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}