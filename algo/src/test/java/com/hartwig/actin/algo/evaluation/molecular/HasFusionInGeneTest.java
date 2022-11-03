package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasFusionInGeneTest {

    @Test
    public void canEvaluate() {
        HasFusionInGene function = new HasFusionInGene("gene A");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusedGene("gene B")));
//        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusedGene("gene A")));
    }
}