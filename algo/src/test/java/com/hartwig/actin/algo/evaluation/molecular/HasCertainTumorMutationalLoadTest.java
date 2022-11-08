package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasCertainTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasCertainTumorMutationalLoad function = new HasCertainTumorMutationalLoad(140, null);
        HasCertainTumorMutationalLoad function2 = new HasCertainTumorMutationalLoad(null, 140);
        HasCertainTumorMutationalLoad function3 = new HasCertainTumorMutationalLoad(140, 280);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEvaluation(EvaluationResult.FAIL, function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEvaluation(EvaluationResult.PASS, function3.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEvaluation(EvaluationResult.PASS, function3.evaluate(MolecularTestFactory.withTumorMutationalLoad(280)));

        assertEvaluation(EvaluationResult.FAIL,
                function3.evaluate(MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQuality(136, true)));
        assertEvaluation(EvaluationResult.WARN,
                function3.evaluate(MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQuality(136, false)));
    }
}