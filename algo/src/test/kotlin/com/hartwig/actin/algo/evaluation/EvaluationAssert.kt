package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat

object EvaluationAssert {

    fun assertMolecularEvaluation(expected: EvaluationResult, actual: Evaluation) {
        assertEvaluation(expected, actual)
        if (actual.result == EvaluationResult.PASS || actual.result == EvaluationResult.WARN) {
            assertThat(actual.inclusionMolecularEvents).isNotEmpty()
        } else {
            assertThat(actual.inclusionMolecularEvents).isEmpty()
        }
    }

    fun assertEvaluation(expected: EvaluationResult, actual: Evaluation) {
        assertThat(actual.result).isEqualTo(expected)
        when (actual.result) {
            EvaluationResult.PASS -> {
                assertThat(actual.passSpecificMessages).isNotEmpty()
                assertThat(actual.undeterminedSpecificMessages).isEmpty()
                assertThat(actual.undeterminedGeneralMessages).isEmpty()
                assertThat(actual.failSpecificMessages).isEmpty()
                assertThat(actual.failGeneralMessages).isEmpty()
            }

            EvaluationResult.WARN -> {
                assertThat(actual.passSpecificMessages).isEmpty()
                assertThat(actual.passGeneralMessages).isEmpty()
                assertThat(actual.warnSpecificMessages).isNotEmpty()
                assertThat(actual.undeterminedSpecificMessages).isEmpty()
                assertThat(actual.undeterminedGeneralMessages).isEmpty()
                assertThat(actual.failSpecificMessages).isEmpty()
                assertThat(actual.failGeneralMessages).isEmpty()
            }

            EvaluationResult.UNDETERMINED -> {
                assertThat(actual.passSpecificMessages).isEmpty()
                assertThat(actual.passSpecificMessages).isEmpty()
                assertThat(actual.passGeneralMessages).isEmpty()
                assertThat(actual.warnSpecificMessages).isEmpty()
                assertThat(actual.warnGeneralMessages).isEmpty()
                assertThat(actual.undeterminedSpecificMessages).isNotEmpty()
                assertThat(actual.failSpecificMessages).isEmpty()
                assertThat(actual.failGeneralMessages).isEmpty()
            }

            EvaluationResult.FAIL -> {
                assertThat(actual.passSpecificMessages).isEmpty()
                assertThat(actual.passGeneralMessages).isEmpty()
                assertThat(actual.warnSpecificMessages).isEmpty()
                assertThat(actual.warnGeneralMessages).isEmpty()
                assertThat(actual.undeterminedSpecificMessages).isEmpty()
                assertThat(actual.undeterminedGeneralMessages).isEmpty()
                assertThat(actual.failSpecificMessages).isNotEmpty()
            }

            else -> {}
        }
    }

    fun assertCombinedEvaluation(expected: EvaluationResult, actual: Evaluation, eval1: EvaluationResult, eval2: EvaluationResult) {
        assertThat(actual.result).isEqualTo(expected)
        when {
            eval1 == EvaluationResult.FAIL || eval2 == EvaluationResult.FAIL -> {
                assertThat(actual.passSpecificMessages).isEmpty()
                assertThat(actual.passGeneralMessages).isEmpty()
                assertThat(actual.warnSpecificMessages).isEmpty()
                assertThat(actual.warnGeneralMessages).isEmpty()
                assertThat(actual.undeterminedSpecificMessages).isEmpty()
                assertThat(actual.undeterminedGeneralMessages).isEmpty()
                assertThat(actual.failSpecificMessages).isNotEmpty()
                assertThat(actual.failGeneralMessages).isNotEmpty()
            }

            (eval1 == EvaluationResult.PASS && eval2 == EvaluationResult.WARN) || (eval2 == EvaluationResult.PASS && eval1 == EvaluationResult.WARN) -> {
                assertThat(actual.passSpecificMessages).isNotEmpty()
                assertThat(actual.passGeneralMessages).isNotEmpty()
                assertThat(actual.undeterminedSpecificMessages).isEmpty()
                assertThat(actual.undeterminedGeneralMessages).isEmpty()
                assertThat(actual.warnSpecificMessages).isNotEmpty()
                assertThat(actual.warnGeneralMessages).isNotEmpty()
                assertThat(actual.failSpecificMessages).isEmpty()
                assertThat(actual.failGeneralMessages).isEmpty()
            }

            (eval1 == EvaluationResult.PASS && eval2 == EvaluationResult.UNDETERMINED) || (eval2 == EvaluationResult.PASS && eval1 == EvaluationResult.UNDETERMINED) -> {
                assertThat(actual.passSpecificMessages).isNotEmpty()
                assertThat(actual.passGeneralMessages).isNotEmpty()
                assertThat(actual.undeterminedSpecificMessages).isNotEmpty()
                assertThat(actual.undeterminedGeneralMessages).isNotEmpty()
                assertThat(actual.warnSpecificMessages).isEmpty()
                assertThat(actual.warnGeneralMessages).isEmpty()
                assertThat(actual.failSpecificMessages).isEmpty()
                assertThat(actual.failGeneralMessages).isEmpty()
            }

            (eval1 == EvaluationResult.WARN && eval2 == EvaluationResult.UNDETERMINED) || (eval2 == EvaluationResult.WARN && eval1 == EvaluationResult.UNDETERMINED) -> {
                assertThat(actual.passSpecificMessages).isEmpty()
                assertThat(actual.passGeneralMessages).isEmpty()
                assertThat(actual.warnSpecificMessages).isNotEmpty()
                assertThat(actual.warnGeneralMessages).isNotEmpty()
                assertThat(actual.undeterminedSpecificMessages).isNotEmpty()
                assertThat(actual.undeterminedGeneralMessages).isNotEmpty()
                assertThat(actual.failSpecificMessages).isEmpty()
                assertThat(actual.failGeneralMessages).isEmpty()
            }

            else -> {}
        }
    }
}