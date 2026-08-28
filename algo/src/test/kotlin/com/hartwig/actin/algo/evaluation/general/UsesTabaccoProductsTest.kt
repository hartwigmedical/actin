package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class UsesTabaccoProductsTest {

    @Test
    fun `Should evaluate to undetermined always`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            UsesTobaccoProducts().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Undetermined if tobacco products are used"
        )
    }
}