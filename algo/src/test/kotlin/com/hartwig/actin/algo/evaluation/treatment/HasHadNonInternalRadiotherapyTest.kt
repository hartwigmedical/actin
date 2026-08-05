package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import org.junit.jupiter.api.Test

class HasHadNonInternalRadiotherapyTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).treatment
    private val function = HasHadNonInternalRadiotherapy(labels)

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for non-radiotherapy treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(treatment("other treatment", false))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for internal radiotherapy`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(radiotherapy(true))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for radiotherapy with internal status not specified`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(radiotherapy(null))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for external radiotherapy`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(radiotherapy(false))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    private fun radiotherapy(isInternal: Boolean?): Radiotherapy {
        return Radiotherapy(name = "radiotherapy", isInternal = isInternal)
    }
}