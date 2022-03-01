package com.hartwig.actin.algo.evaluation.pregnancy;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class CanUseAdequateAnticonceptionTest {

    @Test
    public void canEvaluate() {
        CanUseAdequateAnticonception function = new CanUseAdequateAnticonception();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}