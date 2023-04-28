package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.LabValue
import org.junit.Assert
import org.junit.Test

class LabEvaluationTest {
    @Test
    fun canEvaluateVersusMinLLN() {
        val builder = LabTestFactory.builder().refLimitLow(30.0)
        Assert.assertEquals(EvaluationResult.UNDETERMINED, LabEvaluation.evaluateVersusMinLLN(LabTestFactory.builder().build(), 2.0))
        Assert.assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMinLLN(builder.value(80.0).build(), 2.0))
        Assert.assertEquals(EvaluationResult.FAIL, LabEvaluation.evaluateVersusMinLLN(builder.value(50.0).build(), 2.0))
    }

    @Test
    fun canEvaluateVersusMinULN() {
        val builder = LabTestFactory.builder().refLimitUp(50.0)
        Assert.assertEquals(EvaluationResult.UNDETERMINED, LabEvaluation.evaluateVersusMinULN(LabTestFactory.builder().build(), 2.0))
        Assert.assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMinULN(builder.value(40.0).build(), 0.5))
        Assert.assertEquals(EvaluationResult.FAIL, LabEvaluation.evaluateVersusMinULN(builder.value(20.0).build(), 0.5))
    }

    @Test
    fun canEvaluateVersusMaxULN() {
        val builder = LabTestFactory.builder().refLimitUp(50.0)
        Assert.assertEquals(EvaluationResult.UNDETERMINED, LabEvaluation.evaluateVersusMaxULN(LabTestFactory.builder().build(), 2.0))
        Assert.assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMaxULN(builder.value(70.0).build(), 2.0))
        Assert.assertEquals(EvaluationResult.FAIL, LabEvaluation.evaluateVersusMaxULN(builder.value(120.0).build(), 2.0))
    }

    @Test
    fun canUseOverridesForRefLimitUp() {
        val firstCode = LabEvaluation.REF_LIMIT_UP_OVERRIDES.keys.iterator().next()
        val overrideRefLimitUp: Double = LabEvaluation.REF_LIMIT_UP_OVERRIDES[firstCode]!!
        val value: LabValue = LabTestFactory.builder().code(firstCode).value(1.8 * overrideRefLimitUp).build()
        Assert.assertEquals(EvaluationResult.PASS, LabEvaluation.evaluateVersusMaxULN(value, 2.0))
    }
}