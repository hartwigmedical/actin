package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsInactivatedTest {

    @Test
    public void canEvaluate() {
        GeneIsInactivated function = new GeneIsInactivated("gene 1");

        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", false)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", true)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", false)).result());
    }
}