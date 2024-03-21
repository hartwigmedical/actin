package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasHadIntratumoralInjectionTreatmentTest {
    @Test
    fun canEvaluate() {
        val function = HasHadIntratumoralInjectionTreatment()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
    }
}