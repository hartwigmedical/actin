package com.hartwig.actin.algo.evaluation.pregnancy;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class CanUseAdequateAnticonceptionTest {

    @Test
    public void canEvaluate() {
        CanUseAdequateAnticonception function = new CanUseAdequateAnticonception();

        assertEquals(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()).result());
    }
}