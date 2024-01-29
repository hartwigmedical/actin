package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import org.junit.Test
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyOfNameTest {
    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2020, 6, 6)
        val interpreter = WashoutTestFactory.activeFromDate(minDate)
        val function = HasRecentlyReceivedCancerTherapyOfName(setOf("correct"), interpreter)

        // Fail on no medications
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

        // Fail on medication with wrong name
        medications.add(WashoutTestFactory.medication(name = "other", stopDate = minDate.plusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

        // Fail on medication with old date
        medications.add(WashoutTestFactory.medication(name = "correct", stopDate = minDate.minusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

        // Pass on medication with recent date
        medications.add(WashoutTestFactory.medication(name = "correct", stopDate = minDate.plusDays(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }
}