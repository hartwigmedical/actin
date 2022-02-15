package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientTumorMutationalBurdenTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalBurden function = new HasSufficientTumorMutationalBurden(10D);

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(null)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(20D)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(10D)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalBurden(1D)).result());
    }
}