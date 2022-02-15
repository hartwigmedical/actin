package com.hartwig.actin.algo.evaluation.pregnancy;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsBreastfeedingTest {

    @Test
    public void canEvaluate() {
        IsBreastfeeding function = new IsBreastfeeding();

        assertEquals(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}