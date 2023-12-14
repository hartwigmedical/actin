package com.hartwig.actin.algo.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer.summarize
import org.junit.Assert
import org.junit.Test

class EvaluationSummarizerTest {
    @Test
    fun canInterpretAllPossibleEvaluations() {
        val evaluations: MutableList<Evaluation> = Lists.newArrayList()
        for (result in EvaluationResult.values()) {
            evaluations.add(EvaluationTestFactory.withResult(result))
        }
        Assert.assertNotNull(summarize(evaluations))
    }

    @Test
    fun canSummarizeTestData() {
        val match = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val firstTrialEvaluations: List<Evaluation> = Lists.newArrayList(findByTrialId(match, "Test Trial 1").evaluations().values())
        val summary = summarize(firstTrialEvaluations)
        assertEquals(3, summary.count())
        assertEquals(2, summary.passedCount())
        assertEquals(0, summary.warningCount())
        assertEquals(0, summary.failedCount())
        assertEquals(1, summary.undeterminedCount())
        assertEquals(0, summary.notEvaluatedCount())
        assertEquals(0, summary.nonImplementedCount())
    }

    companion object {
        private fun findByTrialId(treatmentMatch: TreatmentMatch, trialIdToFind: String): TrialMatch {
            for (trialMatch in treatmentMatch.trialMatches()) {
                if (trialMatch.identification().trialId().equals(trialIdToFind)) {
                    return trialMatch
                }
            }
            throw IllegalStateException("Could not find trial with id $trialIdToFind")
        }
    }
}