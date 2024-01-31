package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import org.junit.Test

class HasHadAnyCancerTreatmentTest {

    @Test
    fun shouldFailForEmptyTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, HasHadAnyCancerTreatment().evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassForNonEmptyTreatmentHistory() {
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.PASS, HasHadAnyCancerTreatment().evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }
}