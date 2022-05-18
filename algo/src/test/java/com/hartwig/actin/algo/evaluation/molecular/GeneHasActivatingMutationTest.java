package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneHasActivatingMutationTest {

    @Test
    public void canEvaluate() {
        GeneHasActivatingMutation function = new GeneHasActivatingMutation("geneA");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withActivatedGene("geneB")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withActivatedGene("geneA")));
    }
}