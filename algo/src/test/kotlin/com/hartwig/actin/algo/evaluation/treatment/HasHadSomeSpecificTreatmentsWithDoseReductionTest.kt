package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val TREATMENT_TO_MATCH = treatment("Treat1", true)

class HasHadSomeSpecificTreatmentsWithDoseReductionTest {

    val function = HasHadSomeSpecificTreatmentsWithDoseReduction(TREATMENT_TO_MATCH)

    @Test
    fun `Should be undetermined with specific message when patient has received treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(listOf(TREATMENT_TO_MATCH))
        val result = function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))

        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Has received treat1 but unknown if there may have been a dose reduction")
    }

    @Test
    fun `Should be undetermined with other message when patient may have received treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        val result = function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))

        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined if patient may have received treat1 and if there may have been a dose reduction")
    }

    @Test
    fun `Should fail when patient has not received treatment`() {
        val result = function.evaluate(withTreatmentHistory(emptyList()))

        assertEvaluation(EvaluationResult.FAIL, result)
    }
}