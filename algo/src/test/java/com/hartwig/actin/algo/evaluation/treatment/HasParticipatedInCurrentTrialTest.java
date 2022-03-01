package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.treatment.HasParticipatedInCurrentTrial;

import org.junit.Test;

public class HasParticipatedInCurrentTrialTest {

    @Test
    public void canEvaluate() {
        HasParticipatedInCurrentTrial function = new HasParticipatedInCurrentTrial();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}