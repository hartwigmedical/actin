package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasLowRiskOfHemorrhageUponTreatmentTest {
    @Test
    fun canEvaluate() {
        val function = HasLowRiskOfHemorrhageUponTreatment()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}