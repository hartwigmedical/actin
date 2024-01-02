package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import org.junit.Test

class IsEligibleForOnLabelTreatmentTest {

    val function = IsEligibleForOnLabelTreatment()

    @Test
    fun `Should return undetermined for tumor type CUP`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withTumorDetails(
                    TumorTestFactory.builder().primaryTumorLocation("unknown").primaryTumorSubLocation("CUP").build()
                )
            )
        )
    }

    @Test
    fun `Should return undetermined for empty treatment list`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should return not evaluated for non empty treatment list`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("test", true))))
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(withTreatmentHistory(treatments))
        )
    }
}