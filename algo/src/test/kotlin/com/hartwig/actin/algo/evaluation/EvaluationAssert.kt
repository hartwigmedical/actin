package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
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
}