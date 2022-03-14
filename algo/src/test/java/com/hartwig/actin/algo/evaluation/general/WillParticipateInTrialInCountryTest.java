package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class WillParticipateInTrialInCountryTest {

    @Test
    public void canEvaluate() {
        WillParticipateInTrialInCountry netherlands = new WillParticipateInTrialInCountry("The Netherlands");
        assertEvaluation(EvaluationResult.PASS, netherlands.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        WillParticipateInTrialInCountry germany = new WillParticipateInTrialInCountry("Germany");
        assertEvaluation(EvaluationResult.FAIL, germany.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}