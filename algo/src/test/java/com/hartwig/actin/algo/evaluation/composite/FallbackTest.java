package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory;

import org.junit.Test;

public class FallbackTest {

    @Test
    public void canEvaluate() {
        Fallback pass = new Fallback(x -> EvaluationTestFactory.withResult(EvaluationResult.PASS),
                x -> EvaluationTestFactory.withResult(EvaluationResult.FAIL));
        assertEvaluation(EvaluationResult.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Fallback fallback = new Fallback(x -> EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED),
                x -> EvaluationTestFactory.withResult(EvaluationResult.FAIL));
        assertEvaluation(EvaluationResult.FAIL, fallback.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}