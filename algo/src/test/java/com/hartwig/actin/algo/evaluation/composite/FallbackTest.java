package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class FallbackTest {

    @Test
    public void canEvaluate() {
        Fallback pass = new Fallback(x -> Evaluation.PASS, x -> Evaluation.FAIL);
        assertEquals(Evaluation.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Fallback fallback = new Fallback(x -> Evaluation.UNDETERMINED, x -> Evaluation.FAIL);
        assertEquals(Evaluation.FAIL, fallback.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}