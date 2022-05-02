package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasFusionInGeneTest {

    @Test
    public void canEvaluate() {
        HasFusionInGene function = new HasFusionInGene("gene 1");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusionGene("gene 2", "gene 3")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusionGene("gene 1", "gene 2")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusionGene("gene 2", "gene 1")));
    }
}