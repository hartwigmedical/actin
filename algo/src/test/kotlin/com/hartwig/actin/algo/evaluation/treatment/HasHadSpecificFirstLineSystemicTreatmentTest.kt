package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadSpecificFirstLineSystemicTreatmentTest {
    private val matchingTreatment =
        treatmentHistoryEntry(setOf(treatment("matching treatment", categories = setOf(TreatmentCategory.CHEMOTHERAPY), isSystemic = true)))
    private val otherTreatment = treatmentHistoryEntry(setOf(treatment("other treatment", true)))
    private val function = HasHadSpecificFirstLineSystemicTreatment(matchingTreatment.treatments.first())

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient has not received correct treatment`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(otherTreatment))))
    }

    @Test
    fun `Should fail when patient has not received correct treatment as first line treatment`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(otherTreatment.copy(startYear = 2024), matchingTreatment.copy(startYear = 2025))))
        )
    }

    @Test
    fun `Should fail when trial treatment received in first line but with incorrect category`() {
        val firstLineTrialWithOtherTreatmentCategory = treatmentHistoryEntry(
            setOf(treatment("trial", categories = setOf(TreatmentCategory.IMMUNOTHERAPY), isSystemic = true)),
            isTrial = true,
            startYear = 2024
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(firstLineTrialWithOtherTreatmentCategory, otherTreatment.copy(startYear = 2025))))
        )
    }

    @Test
    fun `Should pass when patient has received correct treatment in first line`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(matchingTreatment.copy(startYear = 2024), otherTreatment.copy(startYear = 2025))))
        )
    }

    @Test
    fun `Should pass when patient has received correct treatment in first line and in third line`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        matchingTreatment.copy(startYear = 2023),
                        otherTreatment.copy(startYear = 2024),
                        matchingTreatment.copy(startYear = 2025)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass when patient has only received correct treatment but with unknown date`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(matchingTreatment))))
    }

    @Test
    fun `Should pass when patient has received correct treatment multiple times and no other treatments`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(matchingTreatment, matchingTreatment.copy(startYear = 2025))))
        )
    }

    @Test
    fun `Should evaluate to undetermined when trial treatment received in first line with unknown category`() {
        val firstLineTrial = treatmentHistoryEntry(setOf(treatment("trial", true)), isTrial = true, startYear = 2024)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(firstLineTrial, otherTreatment.copy(startYear = 2025))))
        )
    }

    @Test
    fun `Should evaluate to undetermined when received correct treatment as first treatment and other treatments with unknown date`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(matchingTreatment.copy(startYear = 2025), otherTreatment)))
        )
    }

    @Test
    fun `Should evaluate to undetermined when correct treatment received and another treatment but both with unknown start date`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(matchingTreatment, otherTreatment)))
        )
    }

    @Test
    fun `Should evaluate to undetermined when correct treatment received with unknown start date and another treatment with known start date`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(matchingTreatment, otherTreatment.copy(startYear = 2025))))
        )
    }
}