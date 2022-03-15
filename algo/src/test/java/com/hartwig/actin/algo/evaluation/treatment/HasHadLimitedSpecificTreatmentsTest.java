package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadLimitedSpecificTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSpecificTreatments function = new HasHadLimitedSpecificTreatments(Sets.newHashSet("treatment 1", "treatment 2"), 1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment
        treatments.add(TreatmentTestFactory.builder().name("wrong treatment").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment
        treatments.add(TreatmentTestFactory.builder().name("treatment 2").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

}