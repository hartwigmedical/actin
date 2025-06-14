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

class OrTest {

    @Test
    fun `Should combine evaluations`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithUndetermined(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithWarn(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithWarn(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithWarn(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithFail(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithFail(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithFail(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()))
    }

    @Test
    fun `Should retain MessagesStrings()`() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, index = 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, index = 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, index = 3)
        val function4: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, index = 4)
        val result: Evaluation = Or(listOf(function1, function2, function3, function4)).evaluate(TEST_PATIENT)
        assertThat(result.passMessagesStrings()).hasSize(2)
        assertThat(result.passMessagesStrings()).contains("pass 3")
        assertThat(result.passMessagesStrings()).contains("pass 4")
        assertThat(result.warnMessagesStrings()).hasSize(2)
        assertThat(result.warnMessagesStrings()).contains("warn 3")
        assertThat(result.warnMessagesStrings()).contains("warn 4")
        assertThat(result.failMessagesStrings()).hasSize(2)
        assertThat(result.failMessagesStrings()).contains("fail 3")
        assertThat(result.failMessagesStrings()).contains("fail 4")
        assertThat(result.undeterminedMessagesStrings()).hasSize(2)
        assertThat(result.undeterminedMessagesStrings()).contains("undetermined 3")
        assertThat(result.undeterminedMessagesStrings()).contains("undetermined 4")
    }

    @Test
    fun `Should combine molecular inclusion and exclusion events`() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, includeMolecular = true, index = 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, includeMolecular = true, index = 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, includeMolecular = true, index = 3)
        val result: Evaluation = Or(listOf(function1, function2, function3)).evaluate(TEST_PATIENT)
        assertThat(result.inclusionMolecularEvents).hasSize(2)
        assertThat(result.inclusionMolecularEvents).contains("inclusion event 2")
        assertThat(result.inclusionMolecularEvents).contains("inclusion event 3")
        assertThat(result.exclusionMolecularEvents).hasSize(2)
        assertThat(result.exclusionMolecularEvents).contains("exclusion event 2")
        assertThat(result.exclusionMolecularEvents).contains("exclusion event 3")
    }

    @Test
    fun `Should only take isMissingMolecularResultForEvaluation property status from best evaluation`() {
        val failFunctionWithoutMissingMolecularResult: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, isMissingMolecularResultForEvaluation = true, index = 1)
        val undeterminedFunctionWithMissingMolecularResult: EvaluationFunction =
            CompositeTestFactory.create(EvaluationResult.UNDETERMINED, isMissingMolecularResultForEvaluation = true, index = 2)
        val passFunctionWithoutMissingMolecularResult: EvaluationFunction =
            CompositeTestFactory.create(EvaluationResult.PASS, isMissingMolecularResultForEvaluation = false, index = 3)
        val orWithPassFailAndUndetermined: Evaluation =
            Or(listOf(failFunctionWithoutMissingMolecularResult, undeterminedFunctionWithMissingMolecularResult, passFunctionWithoutMissingMolecularResult)).evaluate(TEST_PATIENT)
        val orWithFailAndUndetermined: Evaluation =
            Or(listOf(failFunctionWithoutMissingMolecularResult, undeterminedFunctionWithMissingMolecularResult)).evaluate(TEST_PATIENT)

        assertThat(orWithPassFailAndUndetermined.isMissingMolecularResultForEvaluation).isFalse()
        assertThat(orWithFailAndUndetermined.isMissingMolecularResultForEvaluation).isTrue()
    }

    @Test
    fun `Should respect recoverable`() {
        val recoverable: EvaluationFunction = CompositeTestFactory.create(recoverable = true, index = 1)
        val unrecoverable: EvaluationFunction = CompositeTestFactory.create(recoverable = false, index = 2)
        val result: Evaluation = Or(listOf(recoverable, unrecoverable)).evaluate(TEST_PATIENT)
        assertThat(result.recoverable).isTrue
        assertThat(result.undeterminedMessagesStrings()).hasSize(2)
        assertThat(result.undeterminedMessagesStrings()).contains("undetermined 1")
        assertThat(result.undeterminedMessagesStrings()).contains("undetermined 2")
    }

    @Test
    fun `Should only retain recoverable fail message if evaluation result is FAIL`() {
        val function1 = CompositeTestFactory.create(EvaluationResult.FAIL, recoverable = true, index = 1)
        val function2 = CompositeTestFactory.create(EvaluationResult.FAIL, recoverable = false, index = 2)
        val result = Or(listOf(function1, function2)).evaluate(TEST_PATIENT)
        assertThat(result.failMessagesStrings()).containsExactly("fail 1")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should crash on no functions to evaluate`() {
        Or(emptyList()).evaluate(TEST_PATIENT)
    }

    companion object {
        private val TEST_PATIENT: PatientRecord = TestPatientFactory.createProperTestPatientRecord()
        private fun combineWithPass(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.pass(), function)
        }

        private fun combineWithWarn(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.warn(), function)
        }

        private fun combineWithFail(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.fail(), function)
        }

        private fun combineWithUndetermined(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.undetermined(), function)
        }

        private fun combineWithNotEvaluated(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.notEvaluated(), function)
        }

        private fun evaluate(function1: EvaluationFunction, function2: EvaluationFunction): Evaluation {
            return Or(listOf(function1, function2)).evaluate(TEST_PATIENT)
        }
    }
}