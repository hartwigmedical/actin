package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class GeneIsAmplifiedTest {

    @Test
    public void canEvaluate() {
        GeneIsAmplified function = new GeneIsAmplified("gene 1");

        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withAmplifiedGene("gene 2")));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withAmplifiedGene("gene 1")));
    }
}