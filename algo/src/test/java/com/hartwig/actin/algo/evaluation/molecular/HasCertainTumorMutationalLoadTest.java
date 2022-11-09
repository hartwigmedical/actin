package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasCertainTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasCertainTumorMutationalLoad function = new HasCertainTumorMutationalLoad(140, null);
        HasCertainTumorMutationalLoad function2 = new HasCertainTumorMutationalLoad(140, 280);

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)));

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertMolecularEvaluation(EvaluationResult.PASS, function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertMolecularEvaluation(EvaluationResult.PASS, function2.evaluate(MolecularTestFactory.withTumorMutationalLoad(280)));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function2.evaluate(MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQuality(136, true)));
        assertMolecularEvaluation(EvaluationResult.WARN,
                function2.evaluate(MolecularTestFactory.withTumorMutationalLoadAndHasSufficientQuality(136, false)));
    }
}