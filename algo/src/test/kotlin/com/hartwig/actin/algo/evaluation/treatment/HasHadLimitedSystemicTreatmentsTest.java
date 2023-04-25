package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadLimitedSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSystemicTreatments function = new HasHadLimitedSystemicTreatments(1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one non-systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one systemic
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one more systemic
        treatments.add(TreatmentTestFactory.builder().name("treatment 2").isSystemic(true).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void cantDetermineInCaseOfAmbiguousTimeline() {
        HasHadLimitedSystemicTreatments function = new HasHadLimitedSystemicTreatments(1);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        treatments.add(TreatmentTestFactory.builder().name("treatment").isSystemic(true).build());
        treatments.add(TreatmentTestFactory.builder().name("treatment").isSystemic(true).build());

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}