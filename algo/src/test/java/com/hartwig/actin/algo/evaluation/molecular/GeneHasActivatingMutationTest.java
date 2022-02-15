package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneHasActivatingMutationTest {

    @Test
    public void canEvaluate() {
        GeneHasActivatingMutation function = new GeneHasActivatingMutation("gene 1");

        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withActivatedGene("gene 2")).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withActivatedGene("gene 1")).result());
    }
}