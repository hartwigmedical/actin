package com.hartwig.actin.soc.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.soc.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.soc.evaluation.TestEvaluationFunctionFactory
import org.junit.Assert
import org.junit.Test

class NotTest {

    @Test
    fun canNegateEvaluation() {
        assertEvaluation(EvaluationResult.FAIL, Not(TestEvaluationFunctionFactory.pass()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.PASS, Not(TestEvaluationFunctionFactory.fail()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.WARN, Not(TestEvaluationFunctionFactory.warn()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.UNDETERMINED, Not(TestEvaluationFunctionFactory.undetermined()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, Not(TestEvaluationFunctionFactory.notImplemented()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(TEST_PATIENT))
    }

    @Test
    fun canFlipMessagesAndMolecularEventsForPass() {
        val passFunction = CompositeTestFactory.create(EvaluationResult.PASS, true)
        val passed = passFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(passFunction).evaluate(TEST_PATIENT)
        Assert.assertEquals(result.recoverable(), passed.recoverable())
        Assert.assertFalse(passed.inclusionMolecularEvents().isEmpty())
        Assert.assertEquals(passed.inclusionMolecularEvents(), result.exclusionMolecularEvents())
        Assert.assertFalse(passed.exclusionMolecularEvents().isEmpty())
        Assert.assertEquals(passed.exclusionMolecularEvents(), result.inclusionMolecularEvents())
        Assert.assertEquals(passed.passSpecificMessages(), result.failSpecificMessages())
        Assert.assertEquals(passed.passGeneralMessages(), result.failGeneralMessages())
        Assert.assertEquals(passed.failSpecificMessages(), result.passSpecificMessages())
        Assert.assertEquals(passed.failGeneralMessages(), result.passGeneralMessages())
        Assert.assertEquals(passed.undeterminedSpecificMessages(), result.undeterminedSpecificMessages())
        Assert.assertEquals(passed.undeterminedGeneralMessages(), result.undeterminedGeneralMessages())
        Assert.assertEquals(passed.warnSpecificMessages(), result.warnSpecificMessages())
        Assert.assertEquals(passed.warnGeneralMessages(), result.warnGeneralMessages())
    }

    @Test
    fun canFlipMessagesAndMolecularEventsForFail() {
        val failFunction = CompositeTestFactory.create(EvaluationResult.FAIL, true)
        val failed = failFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(failFunction).evaluate(TEST_PATIENT)
        Assert.assertEquals(result.recoverable(), failed.recoverable())
        Assert.assertFalse(failed.inclusionMolecularEvents().isEmpty())
        Assert.assertEquals(failed.inclusionMolecularEvents(), result.exclusionMolecularEvents())
        Assert.assertFalse(failed.exclusionMolecularEvents().isEmpty())
        Assert.assertEquals(failed.exclusionMolecularEvents(), result.inclusionMolecularEvents())
        Assert.assertEquals(failed.passSpecificMessages(), result.failSpecificMessages())
        Assert.assertEquals(failed.passGeneralMessages(), result.failGeneralMessages())
        Assert.assertEquals(failed.failSpecificMessages(), result.passSpecificMessages())
        Assert.assertEquals(failed.failGeneralMessages(), result.passGeneralMessages())
        Assert.assertEquals(failed.undeterminedSpecificMessages(), result.undeterminedSpecificMessages())
        Assert.assertEquals(failed.undeterminedGeneralMessages(), result.undeterminedGeneralMessages())
        Assert.assertEquals(failed.warnSpecificMessages(), result.warnSpecificMessages())
        Assert.assertEquals(failed.warnGeneralMessages(), result.warnGeneralMessages())
    }

    @Test
    fun canRetainMessagesAndMolecularEventsForUndetermined() {
        val undeterminedFunction = CompositeTestFactory.create(EvaluationResult.UNDETERMINED, true)
        val undetermined = undeterminedFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(undeterminedFunction).evaluate(TEST_PATIENT)
        Assert.assertEquals(result.recoverable(), undetermined.recoverable())
        Assert.assertFalse(undetermined.inclusionMolecularEvents().isEmpty())
        Assert.assertEquals(undetermined.inclusionMolecularEvents(), result.inclusionMolecularEvents())
        Assert.assertFalse(undetermined.exclusionMolecularEvents().isEmpty())
        Assert.assertEquals(undetermined.exclusionMolecularEvents(), result.exclusionMolecularEvents())
        Assert.assertEquals(undetermined.passSpecificMessages(), result.passSpecificMessages())
        Assert.assertEquals(undetermined.passGeneralMessages(), result.passGeneralMessages())
        Assert.assertEquals(undetermined.failSpecificMessages(), result.failSpecificMessages())
        Assert.assertEquals(undetermined.failGeneralMessages(), result.failGeneralMessages())
        Assert.assertEquals(undetermined.undeterminedSpecificMessages(), result.undeterminedSpecificMessages())
        Assert.assertEquals(undetermined.undeterminedGeneralMessages(), result.undeterminedGeneralMessages())
        Assert.assertEquals(undetermined.warnSpecificMessages(), result.warnSpecificMessages())
        Assert.assertEquals(undetermined.warnGeneralMessages(), result.warnGeneralMessages())
    }

    companion object {
        private val TEST_PATIENT: PatientRecord = TestDataFactory.createProperTestPatientRecord()
    }
}