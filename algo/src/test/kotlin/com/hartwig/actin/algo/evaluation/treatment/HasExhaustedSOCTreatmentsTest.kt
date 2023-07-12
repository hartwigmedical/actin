package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    val function = HasExhaustedSOCTreatments()

    @Test
    fun shouldReturnUndeterminedForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList())))
    }

    @Test
    fun shouldReturnNotEvaluatedForNonEmptyTreatmentList() {
        val treatments = listOf(TreatmentTestFactory.builder().build())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}