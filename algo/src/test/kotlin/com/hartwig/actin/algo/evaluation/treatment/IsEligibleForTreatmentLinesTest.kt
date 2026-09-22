package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.junit.jupiter.api.Test

class IsEligibleForTreatmentLinesTest {
    
    private val function = IsEligibleForTreatmentLines(listOf(2))

    @Test
    fun `Should fail when not eligible for target treatment line`() {
        val patientWithEmptyHistory = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientWithEmptyHistory), "Patient determined to be eligible for line 1")

        val patientWithTwoLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientWithTwoLines), "Patient determined to be eligible for line 3")
    }

    @Test
    fun `Should pass when eligible for target treatment line`() {
        val patientWithOneLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("FOLFOX", true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientWithOneLine), "Patient determined to be eligible for line 2")
    }

    @Test
    fun `Should not count non-systemic treatments when evaluating eligibility`() {
        val patientWithOneNonSystemicLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("RADIOTHERAPY", false)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patientWithOneNonSystemicLine),
            "Patient determined to be eligible for line 1"
        )

        val patientWithOneNonSystemicLineAndOneSystemicLine = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("RADIOTHERAPY", false),
                treatmentHistoryEntry("FOLFOX", true)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(patientWithOneNonSystemicLineAndOneSystemicLine),
            "Patient determined to be eligible for line 2"
        )
    }

    @Test
    fun `Should match against multiple target lines`() {
        val functionForLine2Or3 = IsEligibleForTreatmentLines(listOf(2, 3))

        val patientWithEmptyHistory = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertEvaluation(
            EvaluationResult.FAIL,
            functionForLine2Or3.evaluate(patientWithEmptyHistory),
            "Patient determined to be eligible for line 1"
        )

        val patientWithOneLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("FOLFOX", true)))
        assertEvaluation(
            EvaluationResult.PASS,
            functionForLine2Or3.evaluate(patientWithOneLine),
            "Patient determined to be eligible for line 2"
        )

        val patientWithTwoLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionForLine2Or3.evaluate(patientWithTwoLines),
            "Patient determined to be eligible for line 3"
        )

        val patientWithThreeLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true),
                treatmentHistoryEntry("TRIFLURIDINE+TIPIRACIL", true)
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionForLine2Or3.evaluate(patientWithThreeLines),
            "Patient determined to be eligible for line 4"
        )
    }

    private fun treatmentHistoryEntry(name: String, isSystemic: Boolean) = TreatmentTestFactory.treatmentHistoryEntry(
        setOf(TreatmentTestFactory.treatment(name, isSystemic))
    )
}