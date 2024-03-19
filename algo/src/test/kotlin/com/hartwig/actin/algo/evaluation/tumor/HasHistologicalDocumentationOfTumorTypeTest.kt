package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasHistologicalDocumentationOfTumorTypeTest {
    @Test
    fun canEvaluate() {
        val function = HasHistologicalDocumentationOfTumorType()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
    }
}