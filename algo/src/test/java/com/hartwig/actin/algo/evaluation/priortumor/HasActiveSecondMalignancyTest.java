package com.hartwig.actin.algo.evaluation.priortumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.junit.Test;

public class HasActiveSecondMalignancyTest {

    @Test
    public void canEvaluate() {
        HasActiveSecondMalignancy function = new HasActiveSecondMalignancy();

        // No active prior tumors.
        List<PriorSecondPrimary> priorTumors = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));

        // One prior tumor but inactive
        priorTumors.add(PriorTumorTestFactory.builder().isActive(false).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));

        // One other prior tumor, still active
        priorTumors.add(PriorTumorTestFactory.builder().isActive(true).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));
    }
}