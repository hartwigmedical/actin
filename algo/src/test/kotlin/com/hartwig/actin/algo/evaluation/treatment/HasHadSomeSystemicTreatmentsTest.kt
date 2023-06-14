package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import org.junit.Test

class HasHadSomeSystemicTreatmentsTest {
    @Test
    fun canEvaluate() {
        val function = HasHadSomeSystemicTreatments(1)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one non-systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one systemic
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one more systemic
        treatments.add(TreatmentTestFactory.builder().name("treatment 2").isSystemic(true).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun cantDetermineInCaseOfAmbiguousTimeline() {
        val function = HasHadSomeSystemicTreatments(2)
        val treatments = listOf(
            TreatmentTestFactory.builder().name("treatment").isSystemic(true).build(),
            TreatmentTestFactory.builder().name("treatment").isSystemic(true).build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}