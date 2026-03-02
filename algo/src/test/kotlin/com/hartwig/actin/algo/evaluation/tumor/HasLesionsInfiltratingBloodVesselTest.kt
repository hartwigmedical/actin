package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasLesionsInfiltratingBloodVesselTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val evaluation = HasLesionsInfiltratingBloodVessel().evaluate(TestPatientFactory.createProperTestPatientRecord())
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }
}
