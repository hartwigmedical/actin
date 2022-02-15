package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLimitedTumorMutationalLoadTest {

    @Test
    public void canEvaluate() {
        HasLimitedTumorMutationalLoad function = new HasLimitedTumorMutationalLoad(140);

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(null)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(200)));
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(140)));
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withTumorMutationalLoad(10)));
    }
}