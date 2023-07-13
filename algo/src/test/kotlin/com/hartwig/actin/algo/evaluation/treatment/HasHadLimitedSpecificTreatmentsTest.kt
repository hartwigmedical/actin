package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadLimitedSpecificTreatmentsTest {
    @Test
    fun canEvaluate() {
        val function = HasHadLimitedSpecificTreatments(setOf("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY, 1)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add wrong treatment
        treatments.add(TreatmentTestFactory.builder().name("wrong treatment").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").addCategories(TreatmentCategory.CHEMOTHERAPY).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add unclear treatment with correct category.
        treatments.add(TreatmentTestFactory.builder().name("other treatment").addCategories(TreatmentCategory.CHEMOTHERAPY).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add another correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 2").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        val function = HasHadLimitedSpecificTreatments(setOf("right treatment"), TreatmentCategory.CHEMOTHERAPY, 1)
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()

        // Add correct treatment within trial
        treatments.add(TreatmentTestFactory.builder().name("right treatment").addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add another trial with unclear treatment
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canHandleNoWarnCategory() {
        val function = HasHadLimitedSpecificTreatments(setOf("treatment"), TreatmentCategory.CHEMOTHERAPY, 1)
        val treatments = listOf(
            TreatmentTestFactory.builder().name("treatment").build(), TreatmentTestFactory.builder().name("treatment").build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}