package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory
import org.junit.Assert
import org.junit.Test

class WarnIfTest {

    @Test
    fun canWarnIf() {
        val patient: PatientRecord = TestDataFactory.createProperTestPatientRecord()
        assertEvaluation(EvaluationResult.WARN, WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(patient))
        assertEvaluation(EvaluationResult.WARN, WarnIf(TestEvaluationFunctionFactory.warn()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.fail()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.undetermined()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient))
        assertEvaluation(EvaluationResult.PASS, WarnIf(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient))
    }

    @Test
    fun canMoveMessagesToWarnOnPass() {
        val result: Evaluation = WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(TestDataFactory.createMinimalTestPatientRecord())
        Assert.assertTrue(result.passSpecificMessages().isEmpty())
        Assert.assertTrue(result.passGeneralMessages().isEmpty())
        Assert.assertFalse(result.warnSpecificMessages().isEmpty())
        Assert.assertFalse(result.warnGeneralMessages().isEmpty())
    }
}