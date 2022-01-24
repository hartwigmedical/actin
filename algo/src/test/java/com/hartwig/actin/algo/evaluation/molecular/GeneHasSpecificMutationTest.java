package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class GeneHasSpecificMutationTest {

    @Test
    public void canEvaluate() {
        GeneHasSpecificMutation function = new GeneHasSpecificMutation("gene 1", "mutation 1");

        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withGeneMutation("gene 1", "mutation 2")));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withGeneMutation("gene 2", "mutation 1")));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withGeneMutation("gene 1", "mutation 1")));
    }
}