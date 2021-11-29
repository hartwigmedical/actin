package com.hartwig.actin.algo.evaluation.pregnancy;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class IsPregnantTest {

    @Test
    public void canEvaluate() {
        IsPregnant function = new IsPregnant();

        assertEquals(Evaluation.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}