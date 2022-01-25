package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class GeneIsInactivatedTest {

    @Test
    public void canEvaluate() {
        GeneIsInactivated function = new GeneIsInactivated("gene 1");

        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", true)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", false)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", true)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", false)));
    }
}