package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class TumorBiopsyTakenBeforeInformedConsentTest {

    @Test
    public void canEvaluate() {
        TumorBiopsyTakenBeforeInformedConsent function = new TumorBiopsyTakenBeforeInformedConsent();

        assertEquals(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}