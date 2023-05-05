package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.junit.Test

class HasHadSomeSpecificTreatmentsTest {
    @Test
    fun canEvaluate() {
        val function = HasHadSomeSpecificTreatments(setOf("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY, 1)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add wrong treatment
        treatments.add(TreatmentTestFactory.builder().name("wrong treatment").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add right type
        treatments.add(TreatmentTestFactory.builder().name("right type").addCategories(TreatmentCategory.CHEMOTHERAPY).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Different correct treatment
        val different: List<PriorTumorTreatment> = listOf(TreatmentTestFactory.builder().name("treatment 2").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(different)))
    }

    @Test
    fun canEvaluateWithTrials() {
        val function = HasHadSomeSpecificTreatments(setOf("treatment"), null, 2)
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canHandleNoWarnCategory() {
        val function = HasHadSomeSpecificTreatments(setOf("treatment"), null, 2)
        val treatments = listOf(
            TreatmentTestFactory.builder().name("treatment").build(),
            TreatmentTestFactory.builder().name("treatment").build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}