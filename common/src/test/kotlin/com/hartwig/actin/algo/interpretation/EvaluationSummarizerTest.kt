package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.interpretation.EvaluationSummarizer.summarize
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
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
                failedCount = 1,
                undeterminedCount = 0
            )
        )
    }
}