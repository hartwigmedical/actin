package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.junit.jupiter.api.Test

class HasHadSomeApprovedTreatmentsTest {
    
    @Test
    fun canEvaluate() {
        val function = HasHadSomeApprovedTreatments(1)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())),
            "Has not had approved treatments (no prior tumor treatment)"
        )

        val record = TreatmentTestFactory.withTreatmentHistoryEntry(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record), "Nr of received approved treatments undetermined")
    }
}