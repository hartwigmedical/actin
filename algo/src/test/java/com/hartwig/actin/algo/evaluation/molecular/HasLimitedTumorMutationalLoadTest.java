package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLimitedTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasLimitedTumorMutationalLoad function = new HasLimitedTumorMutationalLoad(140);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(140)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(10)));
    }
}