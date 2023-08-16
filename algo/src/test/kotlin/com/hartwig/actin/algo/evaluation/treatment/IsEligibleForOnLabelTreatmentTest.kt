package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatment
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import org.junit.Test

class IsEligibleForOnLabelTreatmentTest {

    val function = IsEligibleForOnLabelTreatment()

    @Test
    fun shouldReturnUndeterminedForEmptyTreatmentList() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun shouldReturnNotEvaluatedForNonEmptyTreatmentList() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("test", true))))
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(withTreatmentHistory(treatments))
        )
    }
}