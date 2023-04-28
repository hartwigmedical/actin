package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class CurrentlyGetsCYPXInhibitingOrInducingMedicationTest {
    @Test
    fun canEvaluate() {
        val function = CurrentlyGetsCYPXInhibitingOrInducingMedication("CYP9A9")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}