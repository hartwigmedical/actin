package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsDeletedTest {

    @Test
    public void canEvaluate() {
        GeneIsDeleted function = new GeneIsDeleted("gene 1");

        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", true)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", false)));
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", true)));
    }
}