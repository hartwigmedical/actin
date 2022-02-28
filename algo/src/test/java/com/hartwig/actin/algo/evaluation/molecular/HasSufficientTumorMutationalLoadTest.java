package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalLoad function = new HasSufficientTumorMutationalLoad(140);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(140)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(10)));
    }
}