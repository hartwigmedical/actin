package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasHadSomeApprovedTreatmentsTest {
    @Test
    fun canEvaluate() {
        val function = HasHadSomeApprovedTreatments(1)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))

        val record = TreatmentTestFactory.withTreatmentHistoryEntry(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }
}