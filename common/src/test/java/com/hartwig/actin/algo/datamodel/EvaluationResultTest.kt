package com.hartwig.actin.algo.datamodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluationResultTest {
    @Test
    fun canCompareEvaluationResults() {
        assertThat(EvaluationResult.FAIL.isWorseThan(EvaluationResult.PASS)).isTrue
        assertThat(EvaluationResult.WARN.isWorseThan(EvaluationResult.UNDETERMINED)).isTrue
        assertThat(EvaluationResult.NOT_EVALUATED.isWorseThan(EvaluationResult.NOT_IMPLEMENTED)).isFalse
    }

    @Test
    fun noEvaluationResultIsWorseThanItself() {
        for (result in EvaluationResult.values()) {
            assertThat(result.isWorseThan(result)).isFalse
        }
    }
}