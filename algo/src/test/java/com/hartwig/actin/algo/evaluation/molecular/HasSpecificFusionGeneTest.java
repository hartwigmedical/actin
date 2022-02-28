package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSpecificFusionGeneTest {

    @Test
    public void canEvaluate() {
        HasSpecificFusionGene function = new HasSpecificFusionGene("gene 1", "gene 2");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusionGene("gene 2", "gene 1")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusionGene("gene 1", "gene 2")));
    }
}