package com.hartwig.actin.algo.evaluation.lifestyle;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.lifestyle.IsWillingToNotUseContactLenses;

import org.junit.Test;

public class IsWillingToNotUseContactLensesTest {

    @Test
    public void canEvaluate() {
        IsWillingToNotUseContactLenses function = new IsWillingToNotUseContactLenses();

        assertEvaluation(EvaluationResult.PASS_BUT_WARN, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}