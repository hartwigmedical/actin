package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TestEvaluationFactory;

import org.junit.Test;

public class FallbackTest {

    @Test
    public void canEvaluate() {
        Fallback pass = new Fallback(x -> TestEvaluationFactory.withResult(EvaluationResult.PASS),
                x -> TestEvaluationFactory.withResult(EvaluationResult.FAIL));
        assertEquals(EvaluationResult.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()).result());

        Fallback fallback = new Fallback(x -> TestEvaluationFactory.withResult(EvaluationResult.UNDETERMINED),
                x -> TestEvaluationFactory.withResult(EvaluationResult.FAIL));
        assertEquals(EvaluationResult.FAIL, fallback.evaluate(TestDataFactory.createMinimalTestPatientRecord()).result());
    }
}