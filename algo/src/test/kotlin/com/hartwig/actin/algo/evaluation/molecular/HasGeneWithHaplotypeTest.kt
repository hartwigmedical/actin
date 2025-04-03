package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasGeneWithHaplotypeTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasGeneWithHaplotype("gene", "V1/V2").evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}