package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasSufficientLabValueTest {

    @Test
    public void canEvaluate() {
        HasSufficientLabValue function = new HasSufficientLabValue(200D);

        assertEquals(Evaluation.PASS, function.evaluate(LabTestFactory.builder().value(300D).build()));
        assertEquals(Evaluation.FAIL, function.evaluate(LabTestFactory.builder().value(100D).build()));
    }
}