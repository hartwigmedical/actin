package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private const val MATCHING_TREATMENT_NAME = "treatment 1"
private val MATCHING_TREATMENT = treatment(MATCHING_TREATMENT_NAME, true)

class HasHadLimitedWeeksOfSpecificTreatmentTest {

    private val functionWithMaxWeeks = HasHadLimitedWeeksOfSpecificTreatment(MATCHING_TREATMENT, 6)
    private val functionWithoutMaxWeeks = HasHadLimitedWeeksOfSpecificTreatment(treatment(MATCHING_TREATMENT_NAME, true), null)

    @Test
    fun `Should fail for empty treatments`() {
        listOf(functionWithMaxWeeks, functionWithoutMaxWeeks).forEach { function ->
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
        }
    }

    @Test
    fun `Should fail for wrong treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("wrong", true)))
        listOf(functionWithMaxWeeks, functionWithoutMaxWeeks).forEach { function ->
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        }
    }

    @Test
    fun `Should fail when trial treatment with different drug name`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, functionWithoutMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should fail for correct treatment when treatment duration more than max weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            listOf(MATCHING_TREATMENT),
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 6
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithMaxWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail when trial entry without treatment when treatment duration more than max weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true, startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 6)
        assertEvaluation(EvaluationResult.FAIL, functionWithMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function =
            HasHadLimitedWeeksOfSpecificTreatment(treatment(MATCHING_TREATMENT_NAME, false, setOf(TreatmentCategory.TRANSPLANTATION)), null)
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should evaluate to undetermined when trial entry without treatment and weeks are missing`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        listOf(functionWithoutMaxWeeks, functionWithMaxWeeks).forEach { function ->
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        }
    }

    @Test
    fun `Should evaluate to undetermined when trial entries without treatment when trial duration more than max weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true, startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 6)
        assertEvaluation(EvaluationResult.FAIL, functionWithMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined when trial entry without treatment within requested amount of weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true, startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 4)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for correct treatment when weeks are missing`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_TREATMENT), startYear = 2022, startMonth = 3)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should pass for correct treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_TREATMENT))
        assertEvaluation(EvaluationResult.PASS, functionWithoutMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should pass for correct treatment within requested amount of weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_TREATMENT),  startYear = 2022, startMonth = 3, stopYear = 2022, stopMonth = 4)
        assertEvaluation(EvaluationResult.PASS, functionWithMaxWeeks.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }
}