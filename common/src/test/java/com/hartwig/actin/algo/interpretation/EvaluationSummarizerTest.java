package com.hartwig.actin.algo.interpretation;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvaluationSummarizerTest {

    @Test
    public void canInterpretAllPossibleEvaluations() {
        List<Evaluation> evaluations = Lists.newArrayList();
        for (EvaluationResult result : EvaluationResult.values()) {
            evaluations.add(EvaluationTestFactory.withResult(result));
        }
        assertNotNull(EvaluationSummarizer.summarize(evaluations));
    }

    @Test
    public void canSummarizeTestData() {
        TreatmentMatch match = TestTreatmentMatchFactory.createProperTreatmentMatch();
        List<Evaluation> firstTrialEvaluations = Lists.newArrayList(findByTrialId(match, "Test Trial 1").evaluations().values());

        EvaluationSummary summary = EvaluationSummarizer.summarize(firstTrialEvaluations);
        assertEquals(3, summary.count());
        assertEquals(2, summary.passedCount());
        assertEquals(0, summary.warningCount());
        assertEquals(0, summary.failedCount());
        assertEquals(1, summary.undeterminedCount());
        assertEquals(0, summary.notEvaluatedCount());
        assertEquals(0, summary.nonImplementedCount());
    }

    @NotNull
    private static TrialMatch findByTrialId(@NotNull TreatmentMatch treatmentMatch, @NotNull String trialIdToFind) {
        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            if (trialMatch.identification().trialId().equals(trialIdToFind)) {
                return trialMatch;
            }
        }

        throw new IllegalStateException("Could not find trial with id " + trialIdToFind);
    }
}