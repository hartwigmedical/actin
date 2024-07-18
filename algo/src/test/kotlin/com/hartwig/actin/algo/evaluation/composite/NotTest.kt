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
    fun `Should correctly negate evaluation`() {
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
    fun `Should flip messages and molecular events for fail evaluation`() {
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
    fun `Should retain messages and flip molecular events for undetermined evaluation`() {
        val undeterminedFunction = CompositeTestFactory.create(EvaluationResult.UNDETERMINED, true)
        val undetermined = undeterminedFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(undeterminedFunction).evaluate(TEST_PATIENT)
        assertThat(undetermined.recoverable).isEqualTo(result.recoverable)
        assertThat(undetermined.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(undetermined.exclusionMolecularEvents)
        assertThat(undetermined.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(undetermined.inclusionMolecularEvents)
        assertThat(result.passSpecificMessages).isEqualTo(undetermined.passSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(undetermined.passGeneralMessages)
        assertThat(result.failSpecificMessages).isEqualTo(undetermined.failSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(undetermined.failGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(undetermined.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(undetermined.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(undetermined.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(undetermined.warnGeneralMessages)
    }

    @Test
    fun `Should retain messages and flip molecular events for warn evaluation`() {
        val warnFunction = CompositeTestFactory.create(EvaluationResult.WARN, true)
        val warn = warnFunction.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(warnFunction).evaluate(TEST_PATIENT)
        assertThat(warn.recoverable).isEqualTo(result.recoverable)
        assertThat(warn.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(warn.exclusionMolecularEvents)
        assertThat(warn.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(warn.inclusionMolecularEvents)
        assertThat(result.passSpecificMessages).isEqualTo(warn.passSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(warn.passGeneralMessages)
        assertThat(result.failSpecificMessages).isEqualTo(warn.failSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(warn.failGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(warn.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(warn.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(warn.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(warn.warnGeneralMessages)
    }

    @Test
    fun `Should flip messages and molecular events for not evaluated evaluation`() {
        val notEvaluatedEvaluation = CompositeTestFactory.create(EvaluationResult.NOT_EVALUATED, true)
        val notEvaluated = notEvaluatedEvaluation.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(notEvaluatedEvaluation).evaluate(TEST_PATIENT)
        assertThat(notEvaluated.recoverable).isEqualTo(result.recoverable)
        assertThat(notEvaluated.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(notEvaluated.exclusionMolecularEvents)
        assertThat(notEvaluated.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(notEvaluated.inclusionMolecularEvents)
        assertThat(result.passSpecificMessages).isEqualTo(notEvaluated.failSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(notEvaluated.failGeneralMessages)
        assertThat(result.failSpecificMessages).isEqualTo(notEvaluated.passSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(notEvaluated.passGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(notEvaluated.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(notEvaluated.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(notEvaluated.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(notEvaluated.warnGeneralMessages)
    }

    @Test
    fun `Should retain messages and molecular events for not implemented evaluation`() {
        val notImplementedEvaluation = CompositeTestFactory.create(EvaluationResult.NOT_IMPLEMENTED, true)
        val notImplemented = notImplementedEvaluation.evaluate(TEST_PATIENT)
        val result: Evaluation = Not(notImplementedEvaluation).evaluate(TEST_PATIENT)
        assertThat(notImplemented.recoverable).isEqualTo(result.recoverable)
        assertThat(notImplemented.inclusionMolecularEvents).isNotEmpty()
        assertThat(result.inclusionMolecularEvents).isEqualTo(notImplemented.inclusionMolecularEvents)
        assertThat(notImplemented.exclusionMolecularEvents).isNotEmpty()
        assertThat(result.exclusionMolecularEvents).isEqualTo(notImplemented.exclusionMolecularEvents)
        assertThat(result.passSpecificMessages).isEqualTo(notImplemented.passSpecificMessages)
        assertThat(result.passGeneralMessages).isEqualTo(notImplemented.passGeneralMessages)
        assertThat(result.failSpecificMessages).isEqualTo(notImplemented.failSpecificMessages)
        assertThat(result.failGeneralMessages).isEqualTo(notImplemented.failGeneralMessages)
        assertThat(result.undeterminedSpecificMessages).isEqualTo(notImplemented.undeterminedSpecificMessages)
        assertThat(result.undeterminedGeneralMessages).isEqualTo(notImplemented.undeterminedGeneralMessages)
        assertThat(result.warnSpecificMessages).isEqualTo(notImplemented.warnSpecificMessages)
        assertThat(result.warnGeneralMessages).isEqualTo(notImplemented.warnGeneralMessages)
    }

    companion object {
        private val TEST_PATIENT: PatientRecord = TestPatientFactory.createProperTestPatientRecord()
    }
}