package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class TumorBiopsyTakenBeforeInformedConsentTest {

    @Test
    public void canEvaluate() {
        TumorBiopsyTakenBeforeInformedConsent function = new TumorBiopsyTakenBeforeInformedConsent();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}