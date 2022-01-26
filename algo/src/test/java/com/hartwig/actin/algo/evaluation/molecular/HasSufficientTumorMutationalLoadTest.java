package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasSufficientTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasSufficientTumorMutationalLoad function = new HasSufficientTumorMutationalLoad(140);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(140)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(10)));
    }
}