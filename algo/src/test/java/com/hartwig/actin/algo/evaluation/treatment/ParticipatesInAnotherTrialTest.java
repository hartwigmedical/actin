package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.treatment.ParticipatesInAnotherTrial;

import org.junit.Test;

public class ParticipatesInAnotherTrialTest {

    @Test
    public void canEvaluate() {
        ParticipatesInAnotherTrial function = new ParticipatesInAnotherTrial();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}