package com.hartwig.actin.algo.evaluation.pregnancy;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class CanUseAdequateAnticonceptionTest {

    @Test
    public void canEvaluate() {
        CanUseAdequateAnticonception function = new CanUseAdequateAnticonception();

        assertEquals(Evaluation.IGNORED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}