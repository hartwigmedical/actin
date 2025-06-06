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
                assertThat(actual.passMessages).isNotEmpty()
                assertThat(actual.warnMessages).isEmpty()
                assertThat(actual.undeterminedMessages).isEmpty()
                assertThat(actual.failMessages).isEmpty()
            }

            EvaluationResult.WARN -> {
                assertThat(actual.passMessages).isEmpty()
                assertThat(actual.warnMessages).isNotEmpty()
                assertThat(actual.undeterminedMessages).isEmpty()
                assertThat(actual.failMessages).isEmpty()
            }

            EvaluationResult.UNDETERMINED -> {
                assertThat(actual.passMessages).isEmpty()
                assertThat(actual.warnMessages).isEmpty()
                assertThat(actual.undeterminedMessages).isNotEmpty()
                assertThat(actual.failMessages).isEmpty()
            }

            EvaluationResult.FAIL -> {
                assertThat(actual.passMessages).isEmpty()
                assertThat(actual.warnMessages).isEmpty()
                assertThat(actual.undeterminedMessages).isEmpty()
                assertThat(actual.failMessages).isNotEmpty()
            }

            else -> {}
        }
    }
}