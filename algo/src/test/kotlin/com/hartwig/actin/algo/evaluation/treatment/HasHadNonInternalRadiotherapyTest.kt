package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import org.junit.Test


class HasHadNonInternalRadiotherapyTest {

    @Test
    fun `should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `should fail for non-radiotherapy treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(treatment("other treatment", false))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `should fail for internal radiotherapy`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(radiotherapy(true))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `should pass for radiotherapy with internal status not specified`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(radiotherapy(null))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `should pass for external radiotherapy`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(radiotherapy(false))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    companion object {
        private val FUNCTION = HasHadNonInternalRadiotherapy()

        private fun radiotherapy(isInternal: Boolean?): Radiotherapy {
            return ImmutableRadiotherapy.builder().name("radiotherapy").isInternal(isInternal).build()
        }
    }
}