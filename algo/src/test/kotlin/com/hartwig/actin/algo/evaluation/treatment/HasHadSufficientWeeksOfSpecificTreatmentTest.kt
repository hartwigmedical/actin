package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import org.junit.jupiter.api.Test

private const val MATCHING_TREATMENT_NAME = "treatment 1"
private val MATCHING_TREATMENT = treatment(MATCHING_TREATMENT_NAME, true)

class HasHadSufficientWeeksOfSpecificTreatmentTest {

    private val function = HasHadSufficientWeeksOfSpecificTreatment(MATCHING_TREATMENT, 6)

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("wrong", true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should fail for correct treatment when treatment duration less than min weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            listOf(MATCHING_TREATMENT),
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 3
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should evaluate to undetermined when trial entry without treatment and weeks are missing`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should fail when trial entries without treatment when trial duration less than min weeks`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(emptySet(), isTrial = true, startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 3)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined when trial entry without treatment with more than requested amount of weeks`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(emptySet(), isTrial = true, startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 8)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for correct treatment when weeks are missing`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_TREATMENT), startYear = 2022, startMonth = 3)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should pass for correct treatment within requested amount of weeks`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(MATCHING_TREATMENT), startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 8)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for correct treatment received twice below the requested amount of weeks but together this exceeds the min weeks`() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(
            setOf(MATCHING_TREATMENT),
            startYear = 2017,
            startMonth = 3,
            stopYear = 2017,
            stopMonth = 3
        )
        val treatmentHistoryEntry2 = treatmentHistoryEntry(
            setOf(MATCHING_TREATMENT),
            startYear = 2018,
            startMonth = 3,
            stopYear = 2018,
            stopMonth = 3
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2)))
        )
    }
}