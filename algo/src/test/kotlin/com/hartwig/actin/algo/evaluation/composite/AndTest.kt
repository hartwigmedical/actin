package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AndTest {

    @Test
    fun `Should combine evaluations`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithNotEvaluated(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithNotEvaluated(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithPass(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithPass(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithPass(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithPass(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithUndetermined(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithUndetermined(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithUndetermined(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithWarn(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithWarn(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithFail(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()))
    }

    @Test
    fun `Should retain messages`() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, 3)
        val function4: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, 4)
        val result: Evaluation = And(listOf(function1, function2, function3, function4)).evaluate(TEST_PATIENT)
        assertThat(result.passSpecificMessages).hasSize(2)
        assertThat(result.passSpecificMessages).contains("pass specific 1")
        assertThat(result.passSpecificMessages).contains("pass specific 2")
        assertThat(result.passGeneralMessages).hasSize(2)
        assertThat(result.passGeneralMessages).contains("pass general 1")
        assertThat(result.passGeneralMessages).contains("pass general 2")
        assertThat(result.warnSpecificMessages).hasSize(2)
        assertThat(result.warnSpecificMessages).contains("warn specific 1")
        assertThat(result.warnSpecificMessages).contains("warn specific 2")
        assertThat(result.warnGeneralMessages).hasSize(2)
        assertThat(result.warnGeneralMessages).contains("warn general 1")
        assertThat(result.warnGeneralMessages).contains("warn general 2")
        assertThat(result.failSpecificMessages).hasSize(2)
        assertThat(result.failSpecificMessages).contains("fail specific 1")
        assertThat(result.failSpecificMessages).contains("fail specific 2")
        assertThat(result.failGeneralMessages).hasSize(2)
        assertThat(result.failGeneralMessages).contains("fail general 1")
        assertThat(result.failGeneralMessages).contains("fail general 2")
        assertThat(result.undeterminedSpecificMessages).hasSize(2)
        assertThat(result.undeterminedSpecificMessages).contains("undetermined specific 1")
        assertThat(result.undeterminedSpecificMessages).contains("undetermined specific 2")
        assertThat(result.undeterminedGeneralMessages).hasSize(2)
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 1")
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 2")
    }

    @Test
    fun shouldCombinesMolecularInclusionExclusionEvents() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, true, 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, true, 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, true, 3)
        val result: Evaluation = And(listOf(function1, function2, function3)).evaluate(TEST_PATIENT)
        assertThat(result.inclusionMolecularEvents).hasSize(2)
        assertThat(result.inclusionMolecularEvents).contains("inclusion event 1")
        assertThat(result.inclusionMolecularEvents).contains("inclusion event 2")
        assertThat(result.exclusionMolecularEvents).hasSize(2)
        assertThat(result.exclusionMolecularEvents).contains("exclusion event 1")
        assertThat(result.exclusionMolecularEvents).contains("exclusion event 2")
    }

    @Test
    fun properlyRespectsRecoverable() {
        val recoverable: EvaluationFunction = CompositeTestFactory.create(true, 1)
        val unrecoverable: EvaluationFunction = CompositeTestFactory.create(false, 2)
        val result: Evaluation = And(listOf(recoverable, unrecoverable)).evaluate(TEST_PATIENT)
        assertThat(result.recoverable).isFalse
        assertThat(result.undeterminedGeneralMessages).hasSize(1)
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 2")
    }

    @Test(expected = IllegalStateException::class)
    fun crashOnNoFunctionsToEvaluate() {
        And(emptyList()).evaluate(TEST_PATIENT)
    }

    companion object {
        private val TEST_PATIENT: PatientRecord = TestDataFactory.createProperTestPatientRecord()
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

        private fun combineWithNotImplemented(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.notImplemented(), function)
        }

        private fun evaluate(function1: EvaluationFunction, function2: EvaluationFunction): Evaluation {
            return And(listOf(function1, function2)).evaluate(TEST_PATIENT)
        }
    }
}