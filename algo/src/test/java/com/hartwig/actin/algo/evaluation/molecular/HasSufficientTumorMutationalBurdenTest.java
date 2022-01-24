package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasSufficientTumorMutationalBurdenTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalBurden function = new HasSufficientTumorMutationalBurden(10D);

        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(20D)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(10D)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(1D)));
    }
}