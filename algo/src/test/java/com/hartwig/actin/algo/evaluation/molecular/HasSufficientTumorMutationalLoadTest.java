package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalLoad function = new HasSufficientTumorMutationalLoad(140);

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(140)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(10)).result());
    }
}