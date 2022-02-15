package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSpecificFusionGeneTest {

    @Test
    public void canEvaluate() {
        HasSpecificFusionGene function = new HasSpecificFusionGene("gene 1", "gene 2");

        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusionGene("gene 2", "gene 1")).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusionGene("gene 1", "gene 2")).result());
    }
}