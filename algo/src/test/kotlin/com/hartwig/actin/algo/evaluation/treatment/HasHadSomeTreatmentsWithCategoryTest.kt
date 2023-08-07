package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadSomeTreatmentsWithCategoryTest {
    @Test
    fun canEvaluate() {
        val category = TreatmentCategory.TARGETED_THERAPY
        val function = HasHadSomeTreatmentsWithCategory(category, 2)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )

        // Add correct treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(category).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )

        // Add a trial as well
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )

        // Add another correct treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(category).build())
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments))
        )
    }

    @Test
    fun shouldNotCountTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadSomeTreatmentsWithCategory(TreatmentCategory.SURGERY, 1)
        val trial = TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(listOf(trial, trial)))
        )
    }
}