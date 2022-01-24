package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasLimitedTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasLimitedTumorMutationalLoad function = new HasLimitedTumorMutationalLoad(140);

        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(140)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(10)));
    }
}