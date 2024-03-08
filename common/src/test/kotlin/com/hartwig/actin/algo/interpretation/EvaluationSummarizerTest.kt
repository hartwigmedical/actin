package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer.summarize
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluationSummarizerTest {

    @Test
    fun `Should be able to interpret every evaluation result`() {
        val evaluations = EvaluationResult.values().map(EvaluationTestFactory::withResult)
        assertThat(summarize(evaluations)).isNotNull
    }

    @Test
    fun `Should be able to summarize proper treatment match test data`() {
        val match = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val firstTrialEvaluations = match.trialMatches.first { it.identification.trialId == "Test Trial 1" }.evaluations.values
        assertThat(summarize(firstTrialEvaluations)).isEqualTo(
            EvaluationSummary(
                count = 3,
                passedCount = 2,
                undeterminedCount = 1
            )
        )
    }
}