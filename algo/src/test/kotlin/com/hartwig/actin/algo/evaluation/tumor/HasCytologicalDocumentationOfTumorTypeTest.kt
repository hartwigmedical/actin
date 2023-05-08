package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasCytologicalDocumentationOfTumorTypeTest {
    @Test
    fun canEvaluate() {
        val function = HasCytologicalDocumentationOfTumorType()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}