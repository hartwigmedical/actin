package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class FallbackTest {

    @Test
    public void canEvaluate() {
        Fallback pass = new Fallback(x -> EvaluationResult.PASS, x -> EvaluationResult.FAIL);
        assertEquals(EvaluationResult.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Fallback fallback = new Fallback(x -> EvaluationResult.UNDETERMINED, x -> EvaluationResult.FAIL);
        assertEquals(EvaluationResult.FAIL, fallback.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}