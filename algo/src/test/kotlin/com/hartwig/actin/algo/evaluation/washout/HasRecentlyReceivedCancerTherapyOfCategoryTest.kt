package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import org.junit.Test
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyOfCategoryTest {
    @Test
    fun canEvaluate() {
        val referenceDate = LocalDate.of(2020, 6, 6)
        val interpreter = WashoutTestFactory.activeFromDate(referenceDate)
        val function = HasRecentlyReceivedCancerTherapyOfCategory(setOf("correct"), interpreter)

        // Fail on no medications
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

        // Fail on medication with wrong category
        medications.add(WashoutTestFactory.builder().addCategories("other").stopDate(referenceDate.plusDays(1)).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

        // Fail on medication with old date
        medications.add(WashoutTestFactory.builder().addCategories("correct").stopDate(referenceDate.minusDays(1)).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

        // Pass on medication with recent date
        medications.add(WashoutTestFactory.builder().addCategories("correct").stopDate(referenceDate.plusDays(1)).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }
}