package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLungMetastasesTest {

    @Test
    public void canEvaluate() {
        HasLungMetastases function = new HasLungMetastases();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withOtherLesions(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList("Pulmonal"))));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList("Lymph node", "Lung"))));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList())));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList("Lymph node"))));
    }
}