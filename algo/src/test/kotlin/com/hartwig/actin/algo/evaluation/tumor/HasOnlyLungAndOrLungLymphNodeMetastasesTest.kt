package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasOnlyLungAndOrLungLymphNodeMetastasesTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasOnlyLungAndOrLungLymphNodeMetastases().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}