package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientTumorMutationalBurdenTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalBurden function = new HasSufficientTumorMutationalBurden(10D);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(20D)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(10D)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(1D)));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQuality(5D, true)));
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withTumorMutationalBurdenAndHasSufficientQuality(5D, false)));
    }
}