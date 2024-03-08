package com.hartwig.actin.algo.datamodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluationResultTest {

    @Test
    fun `Should be able to rank every unique evaluation result at least in one permutation`() {
        assertThat(EvaluationResult.FAIL.isWorseThan(EvaluationResult.PASS)).isTrue
        assertThat(EvaluationResult.WARN.isWorseThan(EvaluationResult.UNDETERMINED)).isTrue
        assertThat(EvaluationResult.NOT_EVALUATED.isWorseThan(EvaluationResult.NOT_IMPLEMENTED)).isFalse
    }

    @Test
    fun `Should never rank an evaluation result worse than itself`() {
        for (result in EvaluationResult.values()) {
            assertThat(result.isWorseThan(result)).isFalse
        }
    }
}