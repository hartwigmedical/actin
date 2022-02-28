package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsActivatedOrAmplifiedTest {

    @Test
    public void canEvaluate() {
        GeneIsActivatedOrAmplified function = new GeneIsActivatedOrAmplified("gene 1");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withActivatedGene("gene 2")));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withActivatedGene("gene 1")));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withAmplifiedGene("gene 1")));
    }
}