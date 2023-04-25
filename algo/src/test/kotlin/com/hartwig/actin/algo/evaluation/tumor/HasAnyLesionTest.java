package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasAnyLesionTest {

    @Test
    public void canEvaluate() {
        HasAnyLesion function = new HasAnyLesion();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBoneLesions(true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLiverLesions(true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withCnsLesions(true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesions(true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLungLesions(true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withLymphNodeLesions(true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList("other"))));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBoneLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLiverLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withCnsLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLungLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withLymphNodeLesions(false)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withOtherLesions(Lists.newArrayList())));
    }
}