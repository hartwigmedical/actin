package com.hartwig.actin.algo.evaluation.general;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class CanGiveAdequateInformedConsentTest {

    @Test
    public void canEvaluate() {
        CanGiveAdequateInformedConsent function = new CanGiveAdequateInformedConsent();

        assertEquals(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}