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
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.unrecoverableUndetermined()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.unrecoverableUndetermined()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithUndetermined(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.unrecoverableUndetermined()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithWarn(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithWarn(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithWarn(TestEvaluationFunctionFactory.unrecoverableUndetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.fail()))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()))
        assertEvaluation(EvaluationResult.PASS, combineWithFail(TestEvaluationFunctionFactory.pass()))
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithFail(TestEvaluationFunctionFactory.unrecoverableUndetermined()))
        assertEvaluation(EvaluationResult.WARN, combineWithFail(TestEvaluationFunctionFactory.warn()))
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()))
    }

    @Test
    fun `Should retain messages`() {
        val function1: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, index = 1)
        val function2: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, index = 2)
        val function3: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, index = 3)
        val function4: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.PASS, index = 4)
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
    fun `Should only take isMissingGenesForSufficientEvaluation property status from best evaluation`() {
        val failFunctionWithoutMissingGenes: EvaluationFunction = CompositeTestFactory.create(EvaluationResult.FAIL, isMissingGenes = true, index = 1)
        val undeterminedFunctionWithMissingGenes: EvaluationFunction =
            CompositeTestFactory.create(EvaluationResult.UNDETERMINED, isMissingGenes = true, index = 2)
        val passFunctionWithoutMissingGenes: EvaluationFunction =
            CompositeTestFactory.create(EvaluationResult.PASS, isMissingGenes = false, index = 3)
        val orWithPassFailAndUndetermined: Evaluation =
            Or(listOf(failFunctionWithoutMissingGenes, undeterminedFunctionWithMissingGenes, passFunctionWithoutMissingGenes)).evaluate(TEST_PATIENT)
        val orWithFailAndUndetermined: Evaluation =
            Or(listOf(failFunctionWithoutMissingGenes, undeterminedFunctionWithMissingGenes)).evaluate(TEST_PATIENT)

        assertThat(orWithPassFailAndUndetermined.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(orWithFailAndUndetermined.isMissingGenesForSufficientEvaluation).isTrue()
    }

    @Test
    fun `Should respect recoverable`() {
        val recoverable: EvaluationFunction = CompositeTestFactory.create(recoverable = true, index = 1)
        val unrecoverable: EvaluationFunction = CompositeTestFactory.create(recoverable = false, index = 2)
        val result: Evaluation = Or(listOf(recoverable, unrecoverable)).evaluate(TEST_PATIENT)
        assertThat(result.recoverable).isTrue
        assertThat(result.undeterminedGeneralMessages).hasSize(2)
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 1")
        assertThat(result.undeterminedGeneralMessages).contains("undetermined general 2")
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
            return evaluate(TestEvaluationFunctionFactory.unrecoverableUndetermined(), function)
        }

        private fun combineWithNotEvaluated(function: EvaluationFunction): Evaluation {
            return evaluate(TestEvaluationFunctionFactory.notEvaluated(), function)
        }

        private fun evaluate(function1: EvaluationFunction, function2: EvaluationFunction): Evaluation {
            return Or(listOf(function1, function2)).evaluate(TEST_PATIENT)
        }
    }
}