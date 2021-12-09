package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasLabValueWithinRefTest {

    @Test
    public void canEvaluate() {
        HasLabValueWithinRef function = new HasLabValueWithinRef();

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(LabTestFactory.builder().isOutsideRef(null).build()));
        assertEquals(Evaluation.PASS, function.evaluate(LabTestFactory.builder().isOutsideRef(false).build()));
        assertEquals(Evaluation.FAIL, function.evaluate(LabTestFactory.builder().isOutsideRef(true).build()));
    }
}