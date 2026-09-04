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
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patientWithEmptyHistory),
            "Requirements for line 1 are met"
        )

        val patientWithTwoLines = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                treatmentHistoryEntry("FOLFOX", true),
                treatmentHistoryEntry("CETUXIMAB", true)
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patientWithTwoLines),
            "Requirements for line 3 are met"
        )
    }

    @Test
    fun `Should pass when eligible for target treatment line`() {
        val patientWithOneLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("FOLFOX", true)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(patientWithOneLine),
            "Requirements for line 2 are met"
        )
    }

    @Test
    fun `Should not count non-systemic treatments when evaluating eligibility`() {
        val patientWithOneNonSystemicLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("RADIOTHERAPY", false)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patientWithOneNonSystemicLine),
            "Requirements for line 1 are met"
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
            "Requirements for line 2 are met"
        )
    }

    @Test
    fun `Should match against multiple target lines`() {
        val functionForLine2Or3 = IsEligibleForTreatmentLines(listOf(2, 3))

        val patientWithEmptyHistory = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertEvaluation(
            EvaluationResult.FAIL,
            functionForLine2Or3.evaluate(patientWithEmptyHistory),
            "Requirements for line 1 are met"
        )

        val patientWithOneLine = TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry("FOLFOX", true)))
        assertEvaluation(
            EvaluationResult.PASS,
            functionForLine2Or3.evaluate(patientWithOneLine),
            "Requirements for line 2 are met"
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
            "Requirements for line 3 are met"
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
            "Requirements for line 4 are met"
        )
    }

    private fun treatmentHistoryEntry(name: String, isSystemic: Boolean) = TreatmentTestFactory.treatmentHistoryEntry(
        setOf(TreatmentTestFactory.treatment(name, isSystemic))
    )
}