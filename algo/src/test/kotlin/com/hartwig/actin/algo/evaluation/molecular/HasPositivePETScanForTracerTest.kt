package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasPositivePETScanForTracerTest {
   
    @Test
    fun `Should evaluate to undetermined always`() {
        val function = HasPositivePETScanForTracer("PSMA")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}