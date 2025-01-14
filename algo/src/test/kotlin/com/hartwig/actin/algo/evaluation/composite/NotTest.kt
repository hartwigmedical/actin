package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NotTest {

    @Test
    fun `Should correctly negate evaluation result`() {
        assertEvaluation(EvaluationResult.FAIL, Not(TestEvaluationFunctionFactory.pass()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.PASS, Not(TestEvaluationFunctionFactory.fail()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.WARN, Not(TestEvaluationFunctionFactory.warn()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.UNDETERMINED, Not(TestEvaluationFunctionFactory.undetermined()).evaluate(TEST_PATIENT))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(TEST_PATIENT))
    }

    @Test
    fun `Should flip messages and molecular events for pass evaluation`() {
        val passFunction = CompositeTestFactory.create(EvaluationResult.PASS, includeMolecular = true)
        val passed = passFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(passFunction).evaluate(TEST_PATIENT)
        assertMessagesAreFlipped(passed, result)
        assertEventsAreFlipped(passed, result)
    }

    @Test
    fun `Should flip messages and molecular events for fail evaluation`() {
        val failFunction = CompositeTestFactory.create(EvaluationResult.FAIL, includeMolecular = true)
        val failed = failFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(failFunction).evaluate(TEST_PATIENT)
        assertMessagesAreFlipped(failed, result)
        assertEventsAreFlipped(failed, result)
    }

    @Test
    fun `Should retain messages and flip molecular events for undetermined evaluation`() {
        val undeterminedFunction = CompositeTestFactory.create(EvaluationResult.UNDETERMINED, includeMolecular = true)
        val undetermined = undeterminedFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(undeterminedFunction).evaluate(TEST_PATIENT)
        assertMessagesAreRetained(undetermined, result)
        assertEventsAreFlipped(undetermined, result)
    }

    @Test
    fun `Should retain messages and flip molecular events for warn evaluation`() {
        val warnFunction = CompositeTestFactory.create(EvaluationResult.WARN, includeMolecular = true)
        val warn = warnFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(warnFunction).evaluate(TEST_PATIENT)
        assertMessagesAreRetained(warn, result)
        assertEventsAreFlipped(warn, result)
    }

    @Test
    fun `Should flip messages and molecular events for not evaluated evaluation`() {
        val notEvaluatedEvaluation = CompositeTestFactory.create(EvaluationResult.NOT_EVALUATED, includeMolecular = true)
        val notEvaluated = notEvaluatedEvaluation.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(notEvaluatedEvaluation).evaluate(TEST_PATIENT)
        assertMessagesAreFlipped(notEvaluated, result)
        assertEventsAreFlipped(notEvaluated, result)
    }

    @Test
    fun `Should retain isMissingGenesForSufficientEvaluation value`() {
        val function: EvaluationFunction = CompositeTestFactory.create(isMissingGenes = true)
        val result: Evaluation = Not(function).evaluate(TEST_PATIENT)
        assertThat(result.isMissingGenesForSufficientEvaluation).isTrue()
    }

    private fun assertMessagesAreFlipped(evaluation: Evaluation, negatedEvaluation: Evaluation) {
        assertThat(evaluation.recoverable).isEqualTo(negatedEvaluation.recoverable)
        assertThat(negatedEvaluation.passMessages).isEqualTo(evaluation.failMessages)
        assertThat(negatedEvaluation.failMessages).isEqualTo(evaluation.passMessages)
        assertThat(negatedEvaluation.undeterminedMessages).isEqualTo(evaluation.undeterminedMessages)
        assertThat(negatedEvaluation.warnMessages).isEqualTo(evaluation.warnMessages)
    }

    private fun assertMessagesAreRetained(evaluation: Evaluation, negatedEvaluation: Evaluation) {
        assertThat(evaluation.recoverable).isEqualTo(negatedEvaluation.recoverable)
        assertThat(negatedEvaluation.passMessages).isEqualTo(evaluation.passMessages)
        assertThat(negatedEvaluation.failMessages).isEqualTo(evaluation.failMessages)
        assertThat(negatedEvaluation.undeterminedMessages).isEqualTo(evaluation.undeterminedMessages)
        assertThat(negatedEvaluation.warnMessages).isEqualTo(evaluation.warnMessages)
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