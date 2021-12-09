package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasSufficientLabValueLLNTest {

    @Test
    public void canEvaluate() {
        HasSufficientLabValueLLN function = new HasSufficientLabValueLLN(2);

        assertEquals(Evaluation.PASS, function.evaluate(LabTestFactory.builder().value(80D).refLimitLow(35D).build()));
        assertEquals(Evaluation.FAIL, function.evaluate(LabTestFactory.builder().value(100D).refLimitLow(75D).build()));
    }
}