package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsAmplifiedTest {

    @Test
    public void canEvaluate() {
        GeneIsAmplified function = new GeneIsAmplified("geneA");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withAmplifiedGene("geneB")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withAmplifiedGene("geneA")));
    }
}