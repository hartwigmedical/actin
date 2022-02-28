package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneHasSpecificMutationTest {

    @Test
    public void canEvaluate() {
        GeneHasSpecificMutation function = new GeneHasSpecificMutation("gene 1", "mutation 1");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withGeneMutation("gene 1", "mutation 2")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withGeneMutation("gene 2", "mutation 1")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withGeneMutation("gene 1", "mutation 1")));
    }
}