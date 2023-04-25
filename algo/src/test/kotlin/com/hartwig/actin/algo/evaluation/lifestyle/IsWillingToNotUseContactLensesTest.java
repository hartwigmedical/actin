package com.hartwig.actin.algo.evaluation.lifestyle;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsWillingToNotUseContactLensesTest {

    @Test
    public void canEvaluate() {
        IsWillingToNotUseContactLenses function = new IsWillingToNotUseContactLenses();

        assertEvaluation(EvaluationResult.WARN, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}