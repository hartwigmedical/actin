package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    val function = HasExhaustedSOCTreatments()

    @Test
    fun shouldReturnUndeterminedForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldReturnNotEvaluatedForNonEmptyTreatmentList() {
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }
}