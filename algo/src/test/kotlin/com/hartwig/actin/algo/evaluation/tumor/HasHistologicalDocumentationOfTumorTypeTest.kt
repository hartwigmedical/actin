package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasHistologicalDocumentationOfTumorTypeTest {
    @Test
    fun canEvaluate() {
        val function = HasHistologicalDocumentationOfTumorType()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}