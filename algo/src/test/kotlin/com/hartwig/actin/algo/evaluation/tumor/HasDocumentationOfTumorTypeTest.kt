package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasDocumentationOfTumorTypeTest {

    private val function = HasDocumentationOfTumorType("Cytological")

    @Test
    fun `Should pass always`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}