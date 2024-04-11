package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OrTest {
    @Test
    fun canCombineEvaluations() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithUndetermined(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithWarn(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithWarn(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithWarn(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithFail(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithFail(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithFail(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.notImplemented()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithNotImplemented(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithNotImplemented(TestEvaluationFunctionFactory.undetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithNotImplemented(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithNotImplemented(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()))
    }

    @Test
    fun canRetainMessages() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, 3)
        val function4: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, 4)
        val result: Evaluation = Or(listOf(function1, function2, function3, function4)).evaluate(TEST_PATIENT)
        assertThat(result.passSpecificMessages).hasSize(2)
        assertThat(result.passSpecificMessages).contains("pass specific 3")
        assertThat(result.passSpecificMessages).contains("pass specific 4")
        assertThat(result.passGeneralMessages).hasSize(2)
        assertThat(result.passGeneralMessages).contains("pass general 3")
        assertThat(result.passGeneralMessages).contains("pass general 4")
        assertThat(result.warnSpecificMessages).hasSize(2)
        assertThat(result.warnSpecificMessages).contains("warn specific 3")
        assertThat(result.warnSpecificMessages).contains("warn specific 4")
        assertThat(result.warnGeneralMessages).hasSize(2)
        assertThat(result.warnGeneralMessages).contains("warn general 3")
        assertThat(result.warnGeneralMessages).contains("warn general 4")
        assertThat(result.failSpecificMessages).hasSize(2)
        assertThat(result.failSpecificMessages).contains("fail specific 3")
        assertThat(result.failSpecificMessages).contains("fail specific 4")
        assertThat(result.failGeneralMessages).hasSize(2)
        assertThat(result.failGeneralMessages).contains("fail general 3")
        assertThat(result.failGeneralMessages).contains("fail general 4")
        assertThat(result.undeterminedSpecificMessages).hasSize(2)
        assertThat(result.undeterminedSpecificMessages).contains("undetermined specific 3")
        assertThat(result.undeterminedSpecificMessages).contains("undetermined specific 4")
        assertThat(result.undeterminedGeneralMessages).hasSize(2)
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 3")
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 4")
    }

    @Test
    fun combinesMolecularInclusionExclusionEvents() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, true, 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, true, 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, true, 3)
        val result: Evaluation = Or(listOf(function1, function2, function3)).evaluate(TEST_PATIENT)
        assertThat(result.inclusionMolecularEvents).hasSize(2)
        assertThat(result.inclusionMolecularEvents).contains("inclusion event 2")
        assertThat(result.inclusionMolecularEvents).contains("inclusion event 3")
        assertThat(result.exclusionMolecularEvents).hasSize(2)
        assertThat(result.exclusionMolecularEvents).contains("exclusion event 2")
        assertThat(result.exclusionMolecularEvents).contains("exclusion event 3")
    }

    @Test
    fun properlyRespectsRecoverable() {
        val recoverable: EvaluationFunction = CompositeTestFactory.create(true, 1)
        val unrecoverable: EvaluationFunction = CompositeTestFactory.create(false, 2)
        val result: Evaluation = Or(listOf(recoverable, unrecoverable)).evaluate(TEST_PATIENT)
        assertThat(result.recoverable).isTrue
        assertThat(result.undeterminedGeneralMessages).hasSize(2)
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 1")
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 2")
    }

    @Test(expected = IllegalStateException::class)
    fun crashOnNoFunctionsToEvaluate() {
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

        private fun combineWithNotImplemented(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.notImplemented(), function)
        }

        private fun evaluate(function1: EvaluationFunction, function2: EvaluationFunction): Evaluation {
            return Or(listOf(function1, function2)).evaluate(TEST_PATIENT)
        }
    }
}