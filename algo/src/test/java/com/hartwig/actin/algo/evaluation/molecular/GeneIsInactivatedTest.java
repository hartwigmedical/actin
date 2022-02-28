package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsInactivatedTest {

    @Test
    public void canEvaluate() {
        GeneIsInactivated function = new GeneIsInactivated("gene 1");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", true)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", false)));
    }
}