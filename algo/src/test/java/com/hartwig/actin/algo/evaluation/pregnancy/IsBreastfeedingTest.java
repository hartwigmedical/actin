package com.hartwig.actin.algo.evaluation.pregnancy;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class IsBreastfeedingTest {

    @Test
    public void canEvaluate() {
        IsBreastfeeding function = new IsBreastfeeding();

        assertEquals(Evaluation.IGNORED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}