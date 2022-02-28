package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsDeletedTest {

    @Test
    public void canEvaluate() {
        GeneIsDeleted function = new GeneIsDeleted("gene 1");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", true)));
    }
}