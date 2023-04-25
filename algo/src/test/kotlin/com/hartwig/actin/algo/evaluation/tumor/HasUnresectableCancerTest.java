package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Test;

public class HasUnresectableCancerTest {

    @Test
    public void canEvaluate() {
        HasUnresectableCancer function = new HasUnresectableCancer();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIA)));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.III)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.I)));
    }
}