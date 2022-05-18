package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasFusionInGeneTest {

    @Test
    public void canEvaluate() {
        HasFusionInGene function = new HasFusionInGene("geneA");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusedGene("geneB")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusedGene("geneA")));
    }
}