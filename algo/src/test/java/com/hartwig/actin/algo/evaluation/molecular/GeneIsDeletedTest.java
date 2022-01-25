package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class GeneIsDeletedTest {

    @Test
    public void canEvaluate() {
        GeneIsDeleted function = new GeneIsDeleted("gene 1");

        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 2", true)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", false)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withInactivatedGene("gene 1", true)));
    }
}