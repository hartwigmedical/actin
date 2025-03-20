package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasEcgAberrationTest {
    private val function = HasEcgAberration(TestIcdFactory.createTestModel())

    @Test
    fun `Should pass with ECG aberration`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withEcgDescription("with description"))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withEcgDescription(null)))
    }

    @Test
    fun `Should pass with cardiac arrhythmia in history`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ComorbidityTestFactory.withComorbidity(otherCondition(icdMainCode = IcdConstants.CARDIAC_ARRHYTHMIA_BLOCK)))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withEcgDescription(null)))
    }

    @Test
    fun `Should fail with no ECG aberration no cardiac arrhythmia comorbidities`() {
        val record = CardiacFunctionTestFactory.withEcg(null).copy(comorbidities = emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }
}