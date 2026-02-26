package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.jupiter.api.Test

private const val TREATMENT_NAME_MATCHING = "Matching"
private const val TREATMENT_NAME_TEST = "Test"

class HasHadCombinedTreatmentNamesWithCyclesTest {
    
    private val matchingPriorTreatment = treatmentHistoryEntry(TREATMENT_NAME_MATCHING, 11)
    private val testTreatmentWithWrongCycles = treatmentHistoryEntry(TREATMENT_NAME_TEST, 3)
    private val testTreatmentWithNullCycles = treatmentHistoryEntry(TREATMENT_NAME_TEST, null)
    private val nonMatchingTreatment = treatmentHistoryEntry("unknown", 10)

    private val function = HasHadCombinedTreatmentNamesWithCycles(
        listOf(chemotherapyWithName(TREATMENT_NAME_MATCHING), chemotherapyWithName(TREATMENT_NAME_TEST)), 8, 12
    )

    @Test
    fun `Should pass when all query treatment names have at least one match with required cycles`() {
        val treatmentHistory = listOf(
            matchingPriorTreatment,
            treatmentHistoryEntry(TREATMENT_NAME_TEST, 8),
            testTreatmentWithWrongCycles,
            testTreatmentWithNullCycles,
            nonMatchingTreatment
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should return undetermined when any query treatment name has at least one match with null cycles and none with required cycles`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                withTreatmentHistory(
                    listOf(matchingPriorTreatment, testTreatmentWithWrongCycles, testTreatmentWithNullCycles, nonMatchingTreatment)
                )
            )
        )
    }

    @Test
    fun `Should fail when any query treatment name has all matches with known cycle count outside range`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withTreatmentHistory(listOf(matchingPriorTreatment, testTreatmentWithWrongCycles, nonMatchingTreatment))
            )
        )
    }

    @Test
    fun `Should fail when any query treatment name has no matching treatments in history`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withTreatmentHistory(listOf(matchingPriorTreatment, testTreatmentWithWrongCycles, nonMatchingTreatment))
            )
        )
    }

    private fun treatmentHistoryEntry(name: String, numCycles: Int?): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(chemotherapyWithName(name)), numCycles = numCycles)
    }

    private fun chemotherapyWithName(name: String) = TreatmentTestFactory.drugTreatment(name, TreatmentCategory.CHEMOTHERAPY)
}