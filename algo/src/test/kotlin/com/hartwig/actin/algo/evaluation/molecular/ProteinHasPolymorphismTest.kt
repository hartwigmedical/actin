package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class ProteinHasPolymorphismTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            ProteinHasPolymorphism("protein", "V1/V2").evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}