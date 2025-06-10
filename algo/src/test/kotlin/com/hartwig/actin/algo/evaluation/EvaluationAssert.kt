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
                assertThat(actual.passMessagesStrings()).isNotEmpty()
                assertThat(actual.warnMessagesStrings()).isEmpty()
                assertThat(actual.undeterminedMessagesStrings()).isEmpty()
                assertThat(actual.failMessagesStrings()).isEmpty()
            }

            EvaluationResult.WARN -> {
                assertThat(actual.passMessagesStrings()).isEmpty()
                assertThat(actual.warnMessagesStrings()).isNotEmpty()
                assertThat(actual.undeterminedMessagesStrings()).isEmpty()
                assertThat(actual.failMessagesStrings()).isEmpty()
            }

            EvaluationResult.UNDETERMINED -> {
                assertThat(actual.passMessagesStrings()).isEmpty()
                assertThat(actual.warnMessagesStrings()).isEmpty()
                assertThat(actual.undeterminedMessagesStrings()).isNotEmpty()
                assertThat(actual.failMessagesStrings()).isEmpty()
            }

            EvaluationResult.FAIL -> {
                assertThat(actual.passMessagesStrings()).isEmpty()
                assertThat(actual.warnMessagesStrings()).isEmpty()
                assertThat(actual.undeterminedMessagesStrings()).isEmpty()
                assertThat(actual.failMessagesStrings()).isNotEmpty()
            }

            else -> {}
        }
    }
}