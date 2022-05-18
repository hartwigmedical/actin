package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsInactivatedTest {

    @Test
    public void canEvaluate() {
        GeneIsInactivated function = new GeneIsInactivated("geneA");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("geneB")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("geneA")));
    }
}