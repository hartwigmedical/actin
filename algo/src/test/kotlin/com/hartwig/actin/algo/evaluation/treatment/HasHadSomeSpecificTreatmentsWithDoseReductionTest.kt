package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import org.junit.jupiter.api.Test

private val TREATMENT_TO_MATCH = treatment("Treat1", true)

class HasHadSomeSpecificTreatmentsWithDoseReductionTest {

    val function = HasHadSomeSpecificTreatmentsWithDoseReduction(TREATMENT_TO_MATCH)

    @Test
    fun `Should be undetermined with specific message when patient has received treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(listOf(TREATMENT_TO_MATCH))
        val result = function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            result,
            "Treat1 in provided treatments but unknown if there may have been a dose reduction"
        )
    }

    @Test
    fun `Should be undetermined with other message when patient may have received treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        val result = function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            result,
            "Undetermined if treat1 in provided treatments and if there may have been a dose reduction"
        )
    }

    @Test
    fun `Should fail when patient has not received treatment`() {
        val result = function.evaluate(withTreatmentHistory(emptyList()))

        assertEvaluation(EvaluationResult.FAIL, result, "Treat1 not in provided treatments hence no dose reduction")
    }
}