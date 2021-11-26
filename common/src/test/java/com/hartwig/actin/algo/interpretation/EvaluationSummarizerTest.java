package com.hartwig.actin.algo.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;

import org.junit.Test;

public class EvaluationSummarizerTest {

    @Test
    public void canInterpretAllPossibleEvaluations() {
        assertNotNull(EvaluationSummarizer.summarize(Lists.newArrayList(Evaluation.values())));
    }

    @Test
    public void canSummarizeTestData() {
        TreatmentMatch match = TestTreatmentMatchFactory.createProperTreatmentMatch();
        List<Evaluation> firstTrialEvaluations = Lists.newArrayList(match.trialMatches().get(0).evaluations().values());

        EvaluationSummary summary = EvaluationSummarizer.summarize(firstTrialEvaluations);
        assertEquals(3, summary.count());
        assertEquals(2, summary.passedCount());
        assertEquals(0, summary.warningCount());
        assertEquals(0, summary.failedCount());
        assertEquals(0, summary.undeterminedCount());
        assertEquals(1, summary.notEvaluatedCount());
        assertEquals(0, summary.nonImplementedCount());
    }
}