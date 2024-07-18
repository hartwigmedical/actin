package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NotTest {

    @Test
    fun canNegateEvaluation() {
        assertEvaluation(EvaluationResult.FAIL, Not(TestEvaluationFunctionFactory.pass()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.PASS, Not(TestEvaluationFunctionFactory.fail()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.WARN, Not(TestEvaluationFunctionFactory.warn()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.UNDETERMINED, Not(TestEvaluationFunctionFactory.undetermined()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(TEST_PATIENT))
    }

    @Test
    fun canFlipMessagesAndMolecularEventsForPass() {
        val passFunction = CompositeTestFactory.create(EvaluationResult.PASS, true)
        val passed = passFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(passFunction).evaluate(TEST_PATIENT)
        assertThat(passed.recoverable).isEqualTo(result.recoverable)
        assertThat(passed.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(passed.inclusionMolecularEvents)
        assertThat(passed.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(passed.exclusionMolecularEvents)
        assertThat(result.failSpecificMessages).isEqualTo(passed.passSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(passed.passGeneralMessages)
        assertThat(result.passSpecificMessages).isEqualTo(passed.failSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(passed.failGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(passed.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(passed.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(passed.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(passed.warnGeneralMessages)
    }

    @Test
    fun canFlipMessagesAndMolecularEventsForFail() {
        val failFunction = CompositeTestFactory.create(EvaluationResult.FAIL, true)
        val failed = failFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(failFunction).evaluate(TEST_PATIENT)
        assertThat(failed.recoverable).isEqualTo(result.recoverable)
        assertThat(failed.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(failed.inclusionMolecularEvents)
        assertThat(failed.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(failed.exclusionMolecularEvents)
        assertThat(result.failSpecificMessages).isEqualTo(failed.passSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(failed.passGeneralMessages)
        assertThat(result.passSpecificMessages).isEqualTo(failed.failSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(failed.failGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(failed.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(failed.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(failed.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(failed.warnGeneralMessages)
    }

    @Test
    fun canRetainMessagesAndMolecularEventsForUndetermined() {
        val undeterminedFunction = CompositeTestFactory.create(EvaluationResult.UNDETERMINED, true)
        val undetermined = undeterminedFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(undeterminedFunction).evaluate(TEST_PATIENT)
        assertThat(undetermined.recoverable).isEqualTo(result.recoverable)
        assertThat(undetermined.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(undetermined.inclusionMolecularEvents)
        assertThat(undetermined.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(undetermined.exclusionMolecularEvents)
        assertThat(result.passSpecificMessages).isEqualTo(undetermined.passSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(undetermined.passGeneralMessages)
        assertThat(result.failSpecificMessages).isEqualTo(undetermined.failSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(undetermined.failGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(undetermined.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(undetermined.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(undetermined.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(undetermined.warnGeneralMessages)
    }

    companion object {
        private val TEST_PATIENT: PatientRecord = TestPatientFactory.createProperTestPatientRecord()
    }
}