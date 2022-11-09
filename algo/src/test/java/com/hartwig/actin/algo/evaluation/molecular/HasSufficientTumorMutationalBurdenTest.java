package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientTumorMutationalBurdenTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalBurden function = new HasSufficientTumorMutationalBurden(10D);

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(null)));
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(20D)));
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(10D)));
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(1D)));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQuality(5D, true)));
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQuality(5D, false)));
    }
}