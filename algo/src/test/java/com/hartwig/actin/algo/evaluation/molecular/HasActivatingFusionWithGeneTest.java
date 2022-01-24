package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasActivatingFusionWithGeneTest {

    @Test
    public void canEvaluate() {
        HasActivatingFusionWithGene function = new HasActivatingFusionWithGene("gene 1");

        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withFusionGene("gene 2", "gene 3")));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withFusionGene("gene 1", "gene 2")));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withFusionGene("gene 2", "gene 1")));
    }
}