package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.junit.Test

class IsEligibleForTreatmentLinesTest {
    private val function = IsEligibleForTreatmentLines(listOf(2))

    @Test
    fun `Should fail when not eligible for target treatment line`() {
        val patientWithEmptyHistory = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientWithEmptyHistory))

        val patientWithTwoLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientWithTwoLines))
    }

    @Test
    fun `Should pass when eligible for target treatment line`() {
        val patientWithOneLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("FOLFOX", true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientWithOneLine))
    }

    @Test
    fun `Should not count non-systemic treatments when evaluating eligibility`() {
        val patientWithOneNonSystemicLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("RADIOTHERAPY", false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientWithOneNonSystemicLine))

        val patientWithOneNonSystemicLineAndOneSystemicLine = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("RADIOTHERAPY", false),
                treatmentHistoryEntry("FOLFOX", true)
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientWithOneNonSystemicLineAndOneSystemicLine))
    }

    @Test
    fun `Should match against multiple target lines`() {
        val functionForLine2Or3 = IsEligibleForTreatmentLines(listOf(2, 3))

        val patientWithEmptyHistory = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertEvaluation(EvaluationResult.FAIL, functionForLine2Or3.evaluate(patientWithEmptyHistory))

        val patientWithOneLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("FOLFOX", true)))
        assertEvaluation(EvaluationResult.PASS, functionForLine2Or3.evaluate(patientWithOneLine))

        val patientWithTwoLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true)
            )
        )
        assertEvaluation(EvaluationResult.PASS, functionForLine2Or3.evaluate(patientWithTwoLines))

        val patientWithThreeLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true),
                treatmentHistoryEntry("TRIFLURIDINE+TIPIRACIL", true)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionForLine2Or3.evaluate(patientWithThreeLines))
    }

    private fun treatmentHistoryEntry(name: String, isSystemic: Boolean) = TreatmentTestFactory.treatmentHistoryEntry(
        setOf(TreatmentTestFactory.treatment(name, isSystemic))
    )
}