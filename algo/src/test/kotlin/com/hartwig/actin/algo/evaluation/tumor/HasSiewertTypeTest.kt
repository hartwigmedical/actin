package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSiewertTypeTest {

    private val function = HasSiewertType("I")

    @Test
    fun `Should resolve to undetermined always`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Undetermined whether Siewert type is I"
        )
    }
}