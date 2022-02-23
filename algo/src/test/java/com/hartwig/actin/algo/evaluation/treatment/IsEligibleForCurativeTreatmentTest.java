package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsEligibleForCurativeTreatmentTest {

    @Test
    public void canEvaluate() {
        IsEligibleForCurativeTreatment function = new IsEligibleForCurativeTreatment();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}