package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsTreatedInHospitalTest {

    @Test
    public void canEvaluate() {
        IsTreatedInHospital function = new IsTreatedInHospital();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}