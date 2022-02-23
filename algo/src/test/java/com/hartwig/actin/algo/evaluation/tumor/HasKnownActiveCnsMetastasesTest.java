package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasKnownActiveCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownActiveCnsMetastases function = new HasKnownActiveCnsMetastases();

        assertEquals(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withActiveCnsLesions(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveCnsLesions(false)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withActiveCnsLesions(null)).result());
    }
}