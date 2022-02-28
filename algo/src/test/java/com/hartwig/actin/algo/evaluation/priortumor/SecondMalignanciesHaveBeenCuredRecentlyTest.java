package com.hartwig.actin.algo.evaluation.priortumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.junit.Test;

public class SecondMalignanciesHaveBeenCuredRecentlyTest {

    @Test
    public void canEvaluate() {
        SecondMalignanciesHaveBeenCuredRecently function = new SecondMalignanciesHaveBeenCuredRecently();

        // No prior second primaries.
        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries)));

        // Add one inactive prior second primary
        priorSecondPrimaries.add(PriorTumorTestFactory.builder().isActive(false).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries)));

        // Add one active prior second primary
        priorSecondPrimaries.add(PriorTumorTestFactory.builder().isActive(true).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries)));
    }
}