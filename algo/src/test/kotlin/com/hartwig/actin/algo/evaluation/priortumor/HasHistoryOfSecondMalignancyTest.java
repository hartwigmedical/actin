package com.hartwig.actin.algo.evaluation.priortumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.junit.Test;

public class HasHistoryOfSecondMalignancyTest {

    @Test
    public void canEvaluate() {
        HasHistoryOfSecondMalignancy function = new HasHistoryOfSecondMalignancy();

        // No active prior tumors.
        List<PriorSecondPrimary> priorTumors = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));

        // One prior tumor
        priorTumors.add(PriorTumorTestFactory.builder().build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)));
    }
}