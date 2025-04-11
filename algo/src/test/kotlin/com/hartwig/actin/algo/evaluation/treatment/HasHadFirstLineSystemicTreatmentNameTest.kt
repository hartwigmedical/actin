package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import org.junit.Test

class HasHadFirstLineSystemicTreatmentNameTest {
    private val matchingTreatmentName = "treatment 1"
    private val function = HasHadFirstLineSystemicTreatmentName(treatment(matchingTreatmentName, isSystemic = true))

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient has not received correct treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("wrong", true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should fail when patient has not received correct treatment as first line treatment`() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(setOf(treatment("other treatment", true)), startYear = 2024)
        val treatmentHistoryEntry2 = treatmentHistoryEntry(setOf(treatment(matchingTreatmentName, true)), startYear = 2025)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2)))
        )
    }

    @Test
    fun `Should pass when patient has received correct treatment in first line`() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(setOf(treatment(matchingTreatmentName, true)), startYear = 2024)
        val treatmentHistoryEntry2 = treatmentHistoryEntry(setOf(treatment("other treatment", true)), startYear = 2025)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2)))
        )
    }

    @Test
    fun `Should pass when patient has only received correct treatment but with unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment(matchingTreatmentName, true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined when trial treatment received in first line`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("trial", true)), isTrial = true, startYear = 2025)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined when correct treatment received as first treatment but other treatments with unknown date`() {
        val correctTreatment = treatmentHistoryEntry(setOf(treatment(matchingTreatmentName, true)), startYear = 2025, startMonth = 3)
        val otherTreatmentWithUnknownDate = treatmentHistoryEntry(setOf(treatment("other treatment", true)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(correctTreatment, otherTreatmentWithUnknownDate)))
        )
    }

    @Test
    fun `Should evaluate to undetermined when correct treatment received and another treatment but both with unknown start date`() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(setOf(treatment(matchingTreatmentName, true)))
        val treatmentHistoryEntry2 = treatmentHistoryEntry(setOf(treatment("other treatment", true)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2)))
        )
    }
}