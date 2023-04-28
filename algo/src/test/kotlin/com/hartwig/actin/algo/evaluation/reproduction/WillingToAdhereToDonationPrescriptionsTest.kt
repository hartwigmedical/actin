package com.hartwig.actin.algo.evaluation.reproduction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class WillingToAdhereToDonationPrescriptionsTest {

    @Test
    public void canEvaluate() {
        WillingToAdhereToDonationPrescriptions function = new WillingToAdhereToDonationPrescriptions();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}