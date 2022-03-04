package com.hartwig.actin.algo.evaluation.reproduction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Ignore;
import org.junit.Test;

public class IsBreastfeedingTest {

    @Test
    @Ignore //TODO Fix test
    public void canEvaluate() {
        IsBreastfeeding function = new IsBreastfeeding();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}