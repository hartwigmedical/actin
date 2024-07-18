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
    fun `Should correctly negate evaluation result`() {
        assertEvaluation(EvaluationResult.FAIL, Not(TestEvaluationFunctionFactory.pass()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.PASS, Not(TestEvaluationFunctionFactory.fail()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.WARN, Not(TestEvaluationFunctionFactory.warn()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.UNDETERMINED, Not(TestEvaluationFunctionFactory.undetermined()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, Not(TestEvaluationFunctionFactory.notImplemented()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(TEST_PATIENT))
    }

    @Test
    fun `Should flip messages and molecular events for pass evaluation`() {
        val passFunction = CompositeTestFactory.create(EvaluationResult.PASS, true)
        val passed = passFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(passFunction).evaluate(TEST_PATIENT)
        assertMessagesAreFlipped(passed, result)
        assertEventsAreFlipped(passed, result)
    }

    @Test
    fun `Should flip messages and molecular events for fail evaluation`() {
        val failFunction = CompositeTestFactory.create(EvaluationResult.FAIL, true)
        val failed = failFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(failFunction).evaluate(TEST_PATIENT)
        assertMessagesAreFlipped(failed, result)
        assertEventsAreFlipped(failed, result)
    }

    @Test
    fun `Should retain messages and flip molecular events for undetermined evaluation`() {
        val undeterminedFunction = CompositeTestFactory.create(EvaluationResult.UNDETERMINED, true)
        val undetermined = undeterminedFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(undeterminedFunction).evaluate(TEST_PATIENT)
        assertMessagesAreRetained(undetermined, result)
        assertEventsAreFlipped(undetermined, result)
    }

    @Test
    fun `Should retain messages and flip molecular events for warn evaluation`() {
        val warnFunction = CompositeTestFactory.create(EvaluationResult.WARN, true)
        val warn = warnFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(warnFunction).evaluate(TEST_PATIENT)
        assertMessagesAreRetained(warn, result)
        assertEventsAreFlipped(warn, result)
    }

    @Test
    fun `Should flip messages and molecular events for not evaluated evaluation`() {
        val notEvaluatedEvaluation = CompositeTestFactory.create(EvaluationResult.NOT_EVALUATED, true)
        val notEvaluated = notEvaluatedEvaluation.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(notEvaluatedEvaluation).evaluate(TEST_PATIENT)
        assertMessagesAreFlipped(notEvaluated, result)
        assertEventsAreFlipped(notEvaluated, result)
    }

    private fun assertMessagesAreFlipped(evaluation: Evaluation, negatedEvaluation: Evaluation) {
        assertThat(evaluation.recoverable).isEqualTo(negatedEvaluation.recoverable)
        assertThat(negatedEvaluation.passSpecificMessages).isEqualTo(evaluation.failSpecificMessages)
        assertThat(negatedEvaluation.passGeneralMessages).isEqualTo(evaluation.failGeneralMessages)
        assertThat(negatedEvaluation.failSpecificMessages).isEqualTo(evaluation.passSpecificMessages)
        assertThat(negatedEvaluation.failGeneralMessages).isEqualTo(evaluation.passGeneralMessages)
        assertThat(negatedEvaluation.undeterminedSpecificMessages).isEqualTo(evaluation.undeterminedSpecificMessages)
        assertThat(negatedEvaluation.undeterminedGeneralMessages).isEqualTo(evaluation.undeterminedGeneralMessages)
        assertThat(negatedEvaluation.warnSpecificMessages).isEqualTo(evaluation.warnSpecificMessages)
        assertThat(negatedEvaluation.warnGeneralMessages).isEqualTo(evaluation.warnGeneralMessages)
    }

    private fun assertMessagesAreRetained(evaluation: Evaluation, negatedEvaluation: Evaluation) {
        assertThat(evaluation.recoverable).isEqualTo(negatedEvaluation.recoverable)
        assertThat(negatedEvaluation.passSpecificMessages).isEqualTo(evaluation.passSpecificMessages)
        assertThat(negatedEvaluation.passGeneralMessages).isEqualTo(evaluation.passGeneralMessages)
        assertThat(negatedEvaluation.failSpecificMessages).isEqualTo(evaluation.failSpecificMessages)
        assertThat(negatedEvaluation.failGeneralMessages).isEqualTo(evaluation.failGeneralMessages)
        assertThat(negatedEvaluation.undeterminedSpecificMessages).isEqualTo(evaluation.undeterminedSpecificMessages)
        assertThat(negatedEvaluation.undeterminedGeneralMessages).isEqualTo(evaluation.undeterminedGeneralMessages)
        assertThat(negatedEvaluation.warnSpecificMessages).isEqualTo(evaluation.warnSpecificMessages)
        assertThat(negatedEvaluation.warnGeneralMessages).isEqualTo(evaluation.warnGeneralMessages)
    }

    private fun assertEventsAreFlipped(evaluation: Evaluation, negatedEvaluation: Evaluation) {
        assertThat(evaluation.inclusionMolecularEvents).isNotEmpty()
        assertThat(negatedEvaluation.inclusionMolecularEvents).isEqualTo(evaluation.exclusionMolecularEvents)
        assertThat(evaluation.exclusionMolecularEvents).isNotEmpty()
        assertThat(negatedEvaluation.exclusionMolecularEvents).isEqualTo(evaluation.inclusionMolecularEvents)
    }

    companion object {
        private val TEST_PATIENT: PatientRecord = TestPatientFactory.createProperTestPatientRecord()
    }
}