package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasHifuAmenableLesionTest {

    @Test
    fun `Should evaluate to undetermined always`() {
        val function = HasHifuAmenableLesion()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "HIFU amenability undetermined"
        )
    }
}